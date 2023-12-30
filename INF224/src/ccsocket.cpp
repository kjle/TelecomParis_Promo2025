//
//  ccsocket: C++ Classes for TCP/IP and UDP Datagram INET Sockets.
//  (c) Eric Lecolinet 2016/2020 - http://www.telecom-paristech.fr/~elc
//

#include <iostream>
#include <cstring>
#include <cstdlib>
#if defined(_WIN32) || defined(_WIN64)
#include <winsock2.h>
#include <ws2tcpip.h>
#pragma comment(lib, "ws2_32.lib")

#else
#include <unistd.h>      // fcntl.h  won't compile without unistd.h !
#include <netinet/tcp.h>
#include <netdb.h>
#endif
#include <fcntl.h>
#include <csignal>
#include "ccsocket.h"

using namespace std;

void Socket::startup() {
#if defined(_WIN32) || defined(_WIN64)
  static bool started = false;
  static WSADATA WSAData;
  if (!started) {
    started = true;
    WSAStartup(MAKEWORD(2, 0), &WSAData);
  }
#endif
}


void Socket::cleanup() {
#if defined(_WIN32) || defined(_WIN64)
  static bool started = false;
  if (!started) {
    started = true;
    WSACleanup();
  }
#endif
}


Socket::Socket(int type) {
  startup();

  // family is AF_INET (AF_UNIX or AF_INET6 not supported)
  // type can be SOCK_STREAM (TCP/IP) or SOCK_DGRAM (datagram connection)
  // protocol is 0 (ie chosen automatically)
  sockfd_ = ::socket(AF_INET, type, 0);

  // ignore SIGPIPES when possible
#if defined(SO_NOSIGPIPE)
  int set = 1;
  setsockopt(sockfd_, SOL_SOCKET, SO_NOSIGPIPE, (void*)&set, sizeof(int));
#endif
}


Socket::Socket(int, SOCKET sockfd) : sockfd_(sockfd) {
  startup();
}

Socket::~Socket() {
  close();
}


// for INET4 sockets
int Socket::setLocalAddress(SOCKADDR_IN& addr, int port) {
  addr = {};
  addr.sin_family = AF_INET;
  addr.sin_port = htons(port);
  addr.sin_addr.s_addr = htonl(INADDR_ANY);
  return 0;
}


// for INET4 sockets
int Socket::setAddress(SOCKADDR_IN& addr, const string& host, int port) {
#if defined(_WIN32) || defined(_WIN64)
  addr = {};
  addr.sin_family = AF_INET;
  addr.sin_port = htons(port);
  inet_pton(AF_INET, host.data(), &addr.sin_addr.s_addr);
#else
  addr = {};
  struct hostent* hent = NULL;
  // gethostbyname() is obsolete!
  if (host.empty() || !(hent = ::gethostbyname(host.c_str()))) return -1; // host not found
  addr.sin_family = AF_INET;
  addr.sin_port = htons(port);
  // NB: data might be misaligned but correct result because memcpy() is used
  ::memcpy(&addr.sin_addr, hent->h_addr_list[0], hent->h_length);
#endif
  return 0;
}


int Socket::bind(int port) {
  if (sockfd_ == INVALID_SOCKET) return InvalidSocket;
  // for INET4 sockets
  SOCKADDR_IN addr;
  setLocalAddress(addr, port);
  // assigns the address specified by addr to sockfd (returns -1 on error, 0 otherwise)
  return ::bind(sockfd_, (const SOCKADDR*)&addr, sizeof(addr));
}


int Socket::bind(const string& host, int port) {
  if (sockfd_ == INVALID_SOCKET) return InvalidSocket;
  // for INET4 sockets
  SOCKADDR_IN addr;
  if (setAddress(addr, host, port) < 0) return UnknownHost;
  // assigns the address specified by addr to sockfd (returns -1 on error, 0 otherwise)
  return ::bind(sockfd_, (const SOCKADDR*)&addr, sizeof(addr));
}


int Socket::connect(const string& host, int port) {
  if (sockfd_ == INVALID_SOCKET) return InvalidSocket;
  // for INET4 sockets
  SOCKADDR_IN addr;
  if (setAddress(addr, host, port) < 0) return UnknownHost;
  // connects sockfd to the address specified by addr (returns -1 on error, 0 otherwise)
  return ::connect(sockfd_, (SOCKADDR*)&addr, sizeof(addr));
}


int Socket::close() {
  int stat = 0;
  if (sockfd_ != INVALID_SOCKET) {
    stat = ::shutdown(sockfd_, 2);  // SHUT_RDWR=2
#if defined(_WIN32) || defined(_WIN64)
    stat += ::closesocket(sockfd_);
#else
    stat += ::close(sockfd_);
#endif
  }
  sockfd_ = INVALID_SOCKET;
  return stat;
}


void Socket::shutdownInput() {
  ::shutdown(sockfd_, 0);
}

void Socket::shutdownOutput() {
  ::shutdown(sockfd_, 1/*SD_SEND*/);
}

#if !defined(_WIN32) && !defined(_WIN64)

int Socket::setReceiveBufferSize(int size) {
  return ::setsockopt(sockfd_, SOL_SOCKET, SO_RCVBUF, &size, sizeof(int));
}

int Socket::setSendBufferSize(int size) {
  return ::setsockopt(sockfd_, SOL_SOCKET, SO_SNDBUF, &size, sizeof(int));
}

int Socket::setReuseAddress(bool state) {
  int set = state;
  return ::setsockopt(sockfd_, SOL_SOCKET, SO_REUSEADDR, &set, sizeof(int));
}

int Socket::setSoLinger(bool on, int time) {
  struct linger l;
  l.l_onoff = on;          // Linger active
  l.l_linger = time;       // How long to linger for
  return ::setsockopt(sockfd_, SOL_SOCKET, SO_LINGER, &l, sizeof(l));
}

int Socket::setSoTimeout(int timeout) {
  struct timeval tv;
  tv.tv_sec = timeout / 1000;             // ms to seconds
  tv.tv_usec = (timeout % 1000) * 1000;   // ms to microseconds
  return ::setsockopt(sockfd_, SOL_SOCKET, SO_RCVTIMEO, &tv, sizeof(tv));
}

int Socket::setTcpNoDelay(bool state) {
  int set = state;
  return ::setsockopt(sockfd_, IPPROTO_TCP, TCP_NODELAY, &set, sizeof(int));
}

#endif


// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


ServerSocket::ServerSocket() {
  Socket::startup();
  sockfd_ = ::socket(AF_INET, SOCK_STREAM, 0);
}

ServerSocket::~ServerSocket() {
  close();
}

Socket* ServerSocket::createSocket(SOCKET sockfd) {
  return new Socket(SOCK_STREAM, sockfd);
}


int ServerSocket::bind(int port, int backlog) {
  if (sockfd_ == INVALID_SOCKET) return Socket::InvalidSocket;

  SOCKADDR_IN addr{};
  addr.sin_family = AF_INET;
  addr.sin_port = htons(port);
  addr.sin_addr.s_addr = INADDR_ANY;

  if (::bind(sockfd_, (SOCKADDR*)&addr, sizeof(addr)) < 0) return -1;

#if defined(_WIN32) || defined(_WIN64)
#else
  socklen_t taille = sizeof addr;
  if (::getsockname(sockfd_, (SOCKADDR*)&addr, &taille) < 0) return -1;
#endif

  // le serveur se met en attente sur le socket d'ecoute
  // listen s'applique seulement aux sockets de type SOCK_STREAM ou SOCK_SEQPACKET.
  if (::listen(sockfd_, backlog) < 0) return -1;
  return 0;
}


int ServerSocket::close() {
  int stat = 0;
  if (sockfd_ != INVALID_SOCKET) {
    // ::shutdown(sockfd, SHUT_RDWR);
#if defined(_WIN32) || defined(_WIN64)
    ::closesocket(sockfd_);
#else
    ::close(sockfd_);
#endif
  }
  sockfd_ = INVALID_SOCKET;
  return stat;
}


Socket* ServerSocket::accept() {
  SOCKET sock_com{};

#if defined(_WIN32) || defined(_WIN64)
  SOCKADDR_IN addr_com;
  int sizeofaddr_ = sizeof(addr_com);
  sock_com = ::accept(sockfd_, (SOCKADDR*)&addr_com, &sizeofaddr_);
  if (sock_com == INVALID_SOCKET) return nullptr;
#else
  // cf. man -s 3n accept, EINTR et EWOULBLOCK ne sont pas geres!
  if ((sock_com = ::accept(sockfd_, NULL, NULL)) < 0) return nullptr;
#endif
  return createSocket(sock_com);
}

#if !defined(_WIN32) && !defined(_WIN64)

int ServerSocket::setReceiveBufferSize(int size) {
  return ::setsockopt(sockfd_, SOL_SOCKET, SO_RCVBUF, &size, sizeof(int));
}


int ServerSocket::setReuseAddress(bool state) {
  int set = state;
  return ::setsockopt(sockfd_, SOL_SOCKET, SO_REUSEADDR, &set, sizeof(int));
}


int ServerSocket::setSoTimeout(int timeout) {
  struct timeval tv;
  tv.tv_sec = timeout / 1000;             // ms to seconds
  tv.tv_usec = (timeout % 1000) * 1000;   // ms to microseconds
  return ::setsockopt(sockfd_, SOL_SOCKET, SO_RCVTIMEO, &tv, sizeof(tv));
}


int ServerSocket::setTcpNoDelay(bool state) {
  // turn off TCP coalescence for INET sockets (useful in some cases to avoid delays)
  int set = state;
  return ::setsockopt(sockfd_, IPPROTO_TCP, TCP_NODELAY, (char*)&set, sizeof(int));
}

#endif

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


struct InputBuffer {
  InputBuffer(size_t size) :
  buffer(new char[size]),
  begin(buffer),
  end(buffer + size), remaining(0) {
  }

  ~InputBuffer() {
    delete[] buffer;
  }
  char* buffer;
  char* begin;
  char* end;
  SOCKSIZE remaining;
};


SocketBuffer::SocketBuffer(Socket* sock, size_t inSize, size_t outSize) :
insize_(inSize),
outsize_(outSize),
insep_(-1),  // means '\r' or '\n' or "\r\n"
outsep_('\n'),
sock_(sock),
in_(nullptr) {
}


SocketBuffer::SocketBuffer(Socket& sock, size_t inSize, size_t outSize)
: SocketBuffer(&sock, inSize, outSize) {}


SocketBuffer::~SocketBuffer() {
  delete in_;
}


void SocketBuffer::setReadSeparator(int separ) {
  insep_ = separ;
}


void SocketBuffer::setWriteSeparator(int separ) {
  outsep_ = separ;
}


SOCKSIZE SocketBuffer::readLine(string& str) {
  str.clear();
  if (!sock_) return Socket::InvalidSocket;
  if (!in_) in_ = new InputBuffer(insize_);

  while (true) {
    if (retrieveLine(str, in_->remaining)) return str.length() + 1;
    // - received > 0: data received
    // - received = 0: nothing received (shutdown or empty message)
    // - received < 0: an error occurred
    SOCKSIZE received = sock_->receive(in_->begin, in_->end - in_->begin);
    if (received <= 0) return received;     // -1 (error) or 0 (shutdown)
    if (retrieveLine(str, received)) return str.length() + 1;
  }
}


bool SocketBuffer::retrieveLine(string& str, SOCKSIZE received) {
  if (received <= 0 || in_->begin > in_->end) {
    in_->begin = in_->buffer;
    return false;
  }

  // search for separator
  char* sep = nullptr;
  int sepLen = 1;

  if (insep_ < 0) {     // means: '\r' or '\n' or "\r\n"
    for (char* p = in_->begin; p < in_->begin + received; ++p) {
      if (*p == '\n') {
        sep = p;
        sepLen = 1;
        break;
      }
      else if (*p == '\r') {
        sep = p;
        if (p < in_->begin + received - 1 && *(p + 1) == '\n') {
          sepLen = 2;
        }
        break;
      }
    }
  }
  else {
    for (char* p = in_->begin; p < in_->begin + received; ++p)
    if (*p == insep_) {
      sep = p;
      break;
    }
  }

  if (sep) {
    str.append(in_->begin, sep - in_->begin);
    in_->remaining = received - (sep + sepLen - in_->begin);
    in_->begin = sep + sepLen;
    return true;
  }
  else {
    str.append(in_->begin, received);
    in_->begin = in_->buffer;
    in_->remaining = 0;
    return false;
  }
}


SOCKSIZE SocketBuffer::writeLine(const string& str) {
  if (!sock_) return Socket::InvalidSocket;

  // a negature value of _outSep means that \r\n must be added
  size_t len = str.length() + (outsep_ < 0 ? 2 : 1);

  // if len is not too large, try to send everything in one block
  if (len <= outsize_) {
    char* buf = (char*)malloc(len);
    ::memcpy(buf, str.c_str(), str.length());
    if (outsep_ >= 0) buf[len - 1] = char(outsep_);
    else {
      buf[len - 2] = '\r';
      buf[len - 1] = '\n';
    }
    auto stat = write(buf, len);
    delete buf;
    return stat;
  }
  else {
    SOCKSIZE sent = write(str.c_str(), str.length());
    char buf[] = {char(outsep_), 0};
    if (outsep_ >= 0) sent += sock_->send(buf, 1);
    else sent += sock_->send("\r\n", 2);
    return sent;
  }
}


SOCKSIZE SocketBuffer::write(const char* s, size_t len) {
  if (!sock_) return Socket::InvalidSocket;
  const char* begin = s;
  const char* end = s + len;
  SOCKSIZE totalSent = 0;

  while (begin < end) {
    // - sent > 0: data sent
    // - sent = 0: file was shutdown
    // - sent < 0: an error occurred
    SOCKSIZE sent = sock_->send(begin, end - begin);
    if (sent <= 0) return sent;     // -1 (error) or 0 (shutdown)
    begin += sent;
    totalSent += sent;
  }
  return totalSent;
}


SOCKSIZE SocketBuffer::read(char* s, size_t len) {
  if (!sock_) return Socket::InvalidSocket;
  char* begin = s;
  char* end = s + len;
  SOCKSIZE totalReceived = 0;

  while (begin < end) {
    SOCKSIZE received = sock_->receive(begin, end - begin);
    if (received <= 0) return received;     // -1 (error) or 0 (shutdown)
    begin += received;
    totalReceived += received;
  }
  return totalReceived;
}

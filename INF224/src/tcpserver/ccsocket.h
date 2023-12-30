//
//  ccsocket: C++ Classes for TCP/IP and UDP Datagram INET Sockets.
//  (c) Eric Lecolinet 2016/2020 - https://www.telecom-paris.fr/~elc
//
//  - Socket: TCP/IP or UDP/Datagram IPv4 socket
//  - ServerSocket: TCP/IP Socket Server
//  - SocketBuffer: preserves record boundaries when exchanging data
//   between TCP/IP sockets.
//

#ifndef ccuty_ccsocket
#define ccuty_ccsocket 1

#include <string>

#if defined(_WIN32) || defined(_WIN64)
#include <winsock2.h>
#define SOCKSIZE int
#define SOCKDATA char

#else
#include <sys/types.h>
#include <sys/socket.h>
#define SOCKET int
#define SOCKADDR struct sockaddr
#define SOCKADDR_IN struct sockaddr_in
#define INVALID_SOCKET -1
#define SOCKSIZE ssize_t
#define SOCKDATA void
#endif

// ignore SIGPIPES when possible
#if defined(MSG_NOSIGNAL)
#  define NO_SIGPIPE_(flags) (flags | MSG_NOSIGNAL)
#else
#  define NO_SIGPIPE_(flags) (flags)
#endif

/** TCP/IP or UDP/Datagram IPv4 socket.
 * AF_INET connections following the IPv4 Internet protocol are supported.
 * @note
 * - ServerSocket should be used on the server side.
 * - SIGPIPE signals are ignored when using Linux, BSD or MACOSX.
 * - TCP/IP sockets do not preserve record boundaries but SocketBuffer solves this problem.
 */
class Socket {
public:
  /// Socket errors.
  /// - Socket::Failed (-1): could not connect, could not bind, etc.
  /// - Socket::InvalidSocket (-2): invalid socket or wrong socket type
  /// - Socket::UnknownHost (-3): could not reach host
  enum Errors { Failed = -1, InvalidSocket = -2, UnknownHost = -3 };

  /// initialisation and cleanup of sockets on Widows.
  /// @note startup is automaticcaly called when a Socket or a ServerSocket is created
  /// @{
  static void startup();
  static void cleanup();
  /// @}

  /// Creates a new Socket.
  /// Creates a AF_INET socket using the IPv4 Internet protocol. Type can be:
  /// - SOCK_STREAM (the default) for TCP/IP connected stream sockets
  /// - SOCK_DGRAM for UDP/datagram sockets (available only or Unix/Linux)
  Socket(int type = SOCK_STREAM);

  /// Creates a Socket from an existing socket file descriptor.
  Socket(int type, SOCKET sockfd);

  /// Destructor (closes the socket).
  ~Socket();

  /// Connects the socket to an address.
  /// Typically used for connecting TCP/IP clients to a ServerSocket.
  /// On Unix/Linux host can be a hostname, on Windows it can only be an IP address.
  /// @return 0 on success or a negative value on error which is one of Socket::Errors
  int connect(const std::string& host, int port);

  /// Assigns the socket to localhost.
  /// @return 0 on success or a negative value on error, see Socket::Errors
  int bind(int port);

  /// Assigns the socket to an IP address.
  /// On Unix/Linux host can be a hostname, on Windows it can only be an IP address.
  /// @return 0 on success or a negative value on error, see Socket::Errors
  int bind(const std::string& host, int port);

  /// Closes the socket.
  int close();

  /// Returns true if the socket has been closed.
  bool isClosed() const { return sockfd_ == INVALID_SOCKET; }

  /// Returns the descriptor of the socket.
  SOCKET descriptor() { return sockfd_; }

  /// Disables further receive operations.
  void shutdownInput();

  /// Disables further send operations.
  void shutdownOutput();

  /// Send sdata to a connected (TCP/IP) socket.
  /// Sends the first _len_ bytes in _buf_.
  /// @return the number of bytes that were sent, or 0 or shutdownInput() was called on the other side,
  /// or Socket::Failed (-1) if an error occured.
  /// @note TCP/IP sockets do not preserve record boundaries, see SocketBuffer.
  SOCKSIZE send(const SOCKDATA* buf, size_t len, int flags = 0) {
    return ::send(sockfd_, buf, len, NO_SIGPIPE_(flags));
  }

  /// Receives data from a connected (TCP/IP) socket.
  /// Reads at most _len_ bytes fand stores them in _buf_.
  /// By default, this function blocks the caller until thre is availbale data.
  /// @return the number of bytes that were received, or 0 or shutdownOutput() was called on the
  /// other side, or Socket::Failed (-1) if an error occured.
  SOCKSIZE receive(SOCKDATA* buf, size_t len, int flags = 0) {
    return ::recv(sockfd_, buf, len, flags);
  }

#if !defined(_WIN32) && !defined(_WIN64)

  /// Sends data to a datagram socket.
  SOCKSIZE sendTo(void const* buf, size_t len, int flags,
                  SOCKADDR const* to, socklen_t addrlen) {
    return ::sendto(sockfd_, buf, len, NO_SIGPIPE_(flags), to, addrlen);
  }

  /// Receives data from datagram socket.
  SOCKSIZE receiveFrom(void* buf, size_t len, int flags,
                       SOCKADDR* from, socklen_t* addrlen) {
    return ::recvfrom(sockfd_, buf, len, flags, from, addrlen);
  }

  /// Set the size of the TCP/IP input buffer.
  int setReceiveBufferSize(int size);

  /// Enable/disable the SO_REUSEADDR socket option.
  int setReuseAddress(bool);

  /// Set the size of the TCP/IP output buffer.
  int setSendBufferSize(int size);

  /// Enable/disable SO_LINGER with the specified linger time in seconds.
  int setSoLinger(bool, int linger);

  /// Enable/disable SO_TIMEOUT with the specified timeout (in milliseconds).
  int setSoTimeout(int timeout);

  /// Enable/disable TCP_NODELAY (turns on/off TCP coalescence).
  int setTcpNoDelay(bool);

  /// Return the size of the TCP/IP input buffer.
  int getReceiveBufferSize() const;

  /// Return SO_REUSEADDR state.
  bool getReuseAddress() const;

  /// Return the size of the TCP/IP output buffer.
  int getSendBufferSize() const;

  /// Return SO_LINGER state and the specified linger time in seconds.
  bool getSoLinger(int& linger) const;

  /// Return SO_TIMEOUT value.
  int getSoTimeout() const;

  /// Return TCP_NODELAY state.
  bool getTcpNoDelay() const;

#endif

private:
  friend class ServerSocket;

  // Initializes a local INET4 address, returns 0 on success, -1 otherwise.
  int setLocalAddress(SOCKADDR_IN& addr, int port);
  // Initializes a remote INET4 address, returns 0 on success, -1 otherwise.
  int setAddress(SOCKADDR_IN& addr, const std::string& host, int port);

  SOCKET sockfd_{};
  Socket(const Socket&) = delete;
  Socket& operator=(const Socket&) = delete;
  Socket& operator=(Socket&&) = delete;
};



/// TCP/IP IPv4 server socket.
/// Waits for requests to come in over the network.
/// TCP/IP sockets do not preserve record boundaries but SocketBuffer solves this problem.
class ServerSocket {
public:
  /// Creates a listening socket that waits for connection requests by TCP/IP clients.
  ServerSocket();

  ~ServerSocket();

  /// Accepts a new connection request and returns a socket for exchanging data with this client.
  /// This function blocks until there is a connection request.
  /// @return the new Socket or nullptr on error.
  Socket* accept();

  /// Assigns the server socket to localhost.
  /// @return 0 on success or a negative value on error, see  Socket::Errors
  int bind(int port, int backlog = 50);

  /// Closes the socket.
  int close();

  /// Returns true if the socket was closed.
  bool isClosed() const { return sockfd_ == INVALID_SOCKET; }

  /// Returns the descriptor of the socket.
  SOCKET descriptor() { return sockfd_; }

#if !defined(_WIN32) && !defined(_WIN64)

  /// Sets the SO_RCVBUF option to the specified value.
  int setReceiveBufferSize(int size);

  /// Enables/disables the SO_REUSEADDR socket option.
  int setReuseAddress(bool);

  /// Enables/disables SO_TIMEOUT with the specified timeout (in milliseconds).
  int  setSoTimeout(int timeout);

  /// Turns on/off TCP coalescence (useful in some cases to avoid delays).
  int setTcpNoDelay(bool);

#endif

private:
  Socket* createSocket(SOCKET);
  SOCKET sockfd_{};  // listening socket.
  ServerSocket(const ServerSocket&) = delete;
  ServerSocket& operator=(const ServerSocket&) = delete;
  ServerSocket& operator=(ServerSocket&&) = delete;
};


/** Preserves record boundaries when exchanging messages between connected TCP/IP sockets.
 * Ensures that one call to readLine() corresponds to one and exactly one call to writeLine() on the other side.
 * By default, writeLine() adds \n at the end of each message and readLine() searches for \n, \r or \n\r
 * so that it can retreive the entire record.  Beware messages should thus not contain these charecters.
 *
 * @code
 *   int main() {
 *      Socket sock;
 *      SocketBuffer sockbuf(sock);
 *
 *      int status = sock.connect("localhost", 3331);
 *      if (status < 0) {
 *        cerr << "Could not connect" << endl;
 *        return 1;
 *      }
 *
 *      while (cin) {
 *        string request, response;
 *        cout << "Request: ";
 *        getline(cin, request);
 *
 *        if (sockbuf.writeLine(request) < 0) {
 *           cerr << "Could not send message" << endl;
 *           return 2;
 *        }
 *        if (sockbuf.readLine(response) < 0) {
 *           cerr << "Couldn't receive message" << endl;
 *           return 3;
 *        }
 *      }
 *    return 0;
 *  }
 @endcode
 */
class SocketBuffer {
public:
  /// Constructor.
  /// _socket_ must be a connected TCP/IP Socket. It should **not** be deleted as long as the
  /// SocketBuffer is used.
  /// _inputSize_ and _ouputSize_ are the sizes of the buffers that are used internally for exchanging data.
  /// @{
  SocketBuffer(Socket*, size_t inputSize = 8192, size_t ouputSize = 8192);
  SocketBuffer(Socket&, size_t inputSize = 8192, size_t ouputSize = 8192);
  /// @}

  ~SocketBuffer();

  /** Read a message from a connected socket.
   * readLine() receives one (and only one) message sent by writeLine() on the other side,
   * ie, a call to writeLine() corresponds to one and exactly one call to readLine() on the other side.
   * The received data is stored in _message_. This method blocks until the message is fully received.
   *
   * @return The number of bytes that were received or one of the following values:
   * - 0: shutdownOutput() was called on the other side
   * - Socket::Failed (-1): a connection error occured
   * - Socket::InvalidSocket (-2): the socket is invalid.
   * @note the separator (eg \n) is counted in the value returned by readLine().
   */
  SOCKSIZE readLine(std::string& message);

  /** Send a message to a connected socket.
   * writeLine() sends a message that will be received by a single call of readLine() on the other side,
   *
   * @return see readLine()
   * @note if _message_ contains one or several occurences of the separator, readLine() will be
   * called as many times on the other side.
   */
  SOCKSIZE writeLine(const std::string& message);

  /// Reads exactly _len_ bytes from the socket, blocks otherwise.
  /// @return see readLine()
  SOCKSIZE read(char* buffer, size_t len);

  /// Writes _len_ bytes to the socket.
  /// @return see readLine()
  SOCKSIZE write(const char* str, size_t len);

  /// Returns the associated socket.
  Socket* socket() { return sock_; }

  /// Returns/changes the separator used by readLine().
  /// setReadSeparator() changes the symbol used by readLine() to separate successive messages:
  /// - if _separ_ < 0 (the default) readLine() searches for \\n, \\r or \\n\\r.
  /// - if _separ_ >= 0, readLine() searches for this character to separate messages,
  /// @{
  void setReadSeparator(int separ);
  int readSeparator() const { return insep_; }
  // @}

  /// Returns/changes the separator used by writeLine().
  /// setWriteSeparator() changes the character(s) used by writeLine() to separate successive messages:
  /// - if _separ_ < 0 (the default) writeLine() inserts \\n\\r between successive lines.
  /// - if _separ_ >= 0, writeLine() inserts _separ_ between successive lines,
  /// @{
  void setWriteSeparator(int separ);
  int writeSeparator() const { return outsep_; }
  // @}

private:
  SocketBuffer(const SocketBuffer&) = delete;
  SocketBuffer& operator=(const SocketBuffer&) = delete;
  SocketBuffer& operator=(SocketBuffer&&) = delete;

protected:
  bool retrieveLine(std::string& str, SOCKSIZE received);
  size_t insize_{}, outsize_{};
  int insep_{}, outsep_{};
  Socket* sock_{};
  struct InputBuffer* in_{};
};

#endif

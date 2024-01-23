//
//  tcpserver: TCP/IP INET Server.
//  (c) Eric Lecolinet - Telecom ParisTech - 2016.
//  http://www.telecom-paristech.fr/~elc
//

#include <csignal>
#include <iostream>
#include <thread>
#include "tcpserver.h"
using namespace std;

/// Connection with a given client. Each SocketCnx uses a different thread.
class SocketCnx {
public:
  SocketCnx(TCPServer&, Socket*);
  ~SocketCnx();

  void processRequests();

  TCPServer& server_;
  Socket* sock_;
  SocketBuffer* sockbuf_;
  std::thread thread_;
};


SocketCnx::SocketCnx(TCPServer& server, Socket* socket) :
server_(server),
sock_(socket),
sockbuf_(new SocketBuffer(sock_)),
thread_( std::thread([this]{processRequests();}) ) {
  thread_.detach();
}


SocketCnx::~SocketCnx() {
  sock_->close();
  delete sockbuf_;
  delete sock_;
}


// infinite loop that processes incoming requests on a TCPServer::Cnx connection.
void SocketCnx::processRequests() {
  while (true) {
    std::string request, response;

    // read the incoming request sent by the client
    // SocketBuffer::readLine() lit jusqu'au premier délimiteur (qui est supprimé)
    auto received = sockbuf_->readLine(request);

    if (received < 0) {
      server_.error("Read error");
      break;
    }

    if (received == 0) {
      server_.error("Connection closed by client");
      break;
    }

    // processes the request
    if (!server_.callback_) {
      response = "OK";
    }
    // closes the connection with this client if the callback returns false
    else if (!server_.callback_(request, response)) {
      server_.error("Closing connection with client");
      break;
    }

    // a response is always sent to the client (otherwise it might block)
    // writeLine() response folled by a \n delimiter
    auto sent = sockbuf_->writeLine(response);

    if (sent < 0) {
      server_.error("Write error");
      break;
    }

    if (sent == 0) {
      server_.error("Connection closed by client");
      break;
    }
  }

  // free resources and kills thread
  delete this;
}

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

TCPServer::TCPServer(Callback const& callback) :
callback_(callback) {
  // signal(SIGPIPE, SIG_IGN);  // ignore nasty SIGPIPEs
}

TCPServer::~TCPServer() {}

int TCPServer::run(int port) {
  int status = servsock_.bind(port);  // lier le ServerSocket a ce port

  if (status < 0) {
    error("Can't bind on port: " + to_string(port));
    return status;   // returns negative value, see Socket::bind()
  }

  while (true) {
    if (auto* socket = servsock_.accept()) {
      // lance la lecture des messages de ce socket dans un thread
      new SocketCnx(*this, socket);
    }
    else error("input connection failed");
  }
  return 0;  // means OK
}


void TCPServer::error(const string& msg) {
  std::cerr << "TCPServer: " << msg << std::endl;
}

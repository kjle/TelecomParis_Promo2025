//
//  tcpserver: TCP/IP INET Server.
//  (c) Eric Lecolinet - Telecom ParisTech - 2016.
//  http://www.telecom-paristech.fr/~elc
//

#ifndef __tcpserver__
#define __tcpserver__
#include <memory>
#include <string>
#include <functional>
#include "ccsocket.h"

class TCPConnection;
class TCPLock;

/// TCP/IP IPv4 server.
/// Supports TCP/IP AF_INET IPv4 connections with multiple clients. One thread is used per client.
class TCPServer {
public:

  using Callback =
  std::function< bool(std::string const& request, std::string& response) >;

  /// initializes the server.
  /// The callback function will be called each time the server receives a request from a client.
  /// - _request_ contains the data sent by the client
  /// - _response_ will be sent to the client as a response
  /// The connection with the client is closed if the callback returns false.
  TCPServer(Callback const& callback);

  virtual ~TCPServer();

  /// Starts the server.
  /// Binds an internal ServerSocket to _port_ then starts an infinite loop that processes connection
  /// requests from clients.
  /// @return 0 on normal termination, or a negative value if the ServerSocket could not be bound
  /// (value is then one of Socket::Errors).
  virtual int run(int port);

private:
  friend class TCPLock;
  friend class SocketCnx;

  TCPServer(TCPServer const&) = delete;
  TCPServer& operator=(TCPServer const&) = delete;
  void error(std::string const& msg);

  ServerSocket servsock_;
  Callback callback_{};
};

#endif

package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.PropertyPermission;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  public static void main(String[] args) {
    int numberThreadPool = 64;
    final int PORT = 8080;

    Server server = new Server(numberThreadPool);
    server.startServer(PORT);

  }
}



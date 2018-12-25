_Submitting:_  
Dafna Shimshi 312196082  
Dan Pollak 308177609

---

_Class List:_
* **Sockspy.java**: The main server class, instantiate the server and
handles the thread allocation for client connections.
* **Socks.java**: This class is used to hold the SOCKS protocol data, parse it and validate it.
* **ProxyConnection.java**: This is a runnable thread that handles both TCP connection for a single SOCKS request.
* **DataPipe.java**: This is a runnable thread that handles data piping between an input and output stream.
* **BasicAuthGrab.java**: This class is used to collect and parse lines from an HTTP request and extract the Basic Authentication parameters.

---

_Implmentation Notes:_

1. We chose to implement the server with threads and not NIO, using executors to handle the thread allocation and queues.
2. Instead of allocating all the threads in the server main executor, we chose to have every connection allocating its own threads.  
This is because we do not want to over allocate SOCKS connections.
3. Since the data streaming is two-way, we implemented a generic Data Pipe thread that only receives Input and Output streams.  
This allows us to run multiple stream reading in either way with code reusability.  
4. We have 2 "Parser" classes - Socks for parsing the socks request, and BasicGrabAuth for the Basic Authentication sniffing.  
We've decided to have the parser classes outside of the threads for readability and debugging. 
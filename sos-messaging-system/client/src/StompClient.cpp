#include <iostream>
#include <thread>
#include <atomic>
#include <mutex>
#include <vector>
#include "../include/ConnectionHandler.h"
#include "../include/StompProtocol.h"

using namespace std;

// Shared resources
std::mutex receive_mutex;
std::mutex send_mutex;
std::atomic<bool> terminated(false); // To signal threads to stop

void ReadFromSocket(StompProtocol &protocol, ConnectionHandler &handler) {
    string frameFromServer;
    while (!terminated) {
        {
            receive_mutex.lock(); // Lock the receive mutex
            frameFromServer.clear();
            if (!handler.getLine(frameFromServer)) {
                cout << "Error reading from server or connection closed." << endl;
                terminated = true; // Stop if connection fails
                send_mutex.unlock(); // Unlock the send mutex
                return;
            }
            string output = protocol.MessageFromServer(frameFromServer);
            cout << output << endl;
            if(protocol.split(output,"\n")[0]=="ERROR"){
                terminated = true; // Signal termination
                handler.close();
                send_mutex.unlock();
                return;
            }
            if (output == "Logout successful") {
                terminated = true; // Signal termination
                handler.close();
                send_mutex.unlock(); // Unlock the send mutex
                return;
            }
        
            send_mutex.unlock(); // Unlock the send mutex
        }
    }
}

int main(int argc, char *argv[]) {
    while (true) {
        terminated = false; // Reset termination flag

        StompProtocol protocol;
        ConnectionHandler connectionHandler(protocol.host, protocol.port);

        string line;
        bool loggedIn = false;

        while (!loggedIn) {
            getline(cin, line);
            if (protocol.split(line, " ")[0] != "login") {
                cout << "please login first" << endl;
                continue;
            }

            string frame = protocol.MessageToServer(line);
            connectionHandler.host_ = protocol.host;
            connectionHandler.port_ = protocol.port;
            if (protocol.split(line, " ")[0] == "login" && !frame.empty()) {
                if (!connectionHandler.connect()) {
                    cerr << "Cannot connect to " << protocol.host << ":" << protocol.port << endl;
                    continue;
                }
                connectionHandler.sendLine(frame);
                string frameFromServer;
                connectionHandler.getLine(frameFromServer);
                string output = protocol.MessageFromServer(frameFromServer);
                cout << output << endl;

                if (output == "Login successful") {
                    loggedIn = true;
                } else {
                    connectionHandler.close();
                }
            }
            else { 
                continue;
            }
        }
        
        // Launch threads
        //send_mutex.lock();
        receive_mutex.lock();
        thread socketThread(ReadFromSocket, ref(protocol), ref(connectionHandler));
        line="";
        while (!terminated) 
        {  
            
            send_mutex.lock(); // Lock the send mutex
            receive_mutex.try_lock();
            if(terminated){
                break;
            }
            getline(cin, line); // Read input from the user

            if(protocol.split(line," ")[0]=="login"){
                cout<< "The client is already logged in, log out before trying again" <<endl;
                send_mutex.unlock();
                continue;
            }


            if(protocol.split(line," ")[0]=="summary"){
                while(connectionHandler.hasMessage()){
                receive_mutex.unlock();
                 std::this_thread::sleep_for(std::chrono::milliseconds(10));
                }
    
            }
                        
            if (line == "logout") {
                 while(connectionHandler.hasMessage()){
                 receive_mutex.unlock();
                 std::this_thread::sleep_for(std::chrono::milliseconds(10));
                }

                string frame = protocol.MessageToServer(line);
                if (!connectionHandler.sendLine(frame)) {
                    cout << "Error sending logout message." << endl;
                }
                terminated = true; // Signal termination
                receive_mutex.unlock(); // Unlock the receive mutex
                continue;
            }

            // Handle other commands
            vector<string> msg = protocol.split(line, " ");
            string frame = protocol.MessageToServer(line);
            if(msg[0]=="summary" && msg.size()==4){
                send_mutex.unlock();
                continue;
            }
            if (msg[0] == "report" && msg.size() == 2) {
                string jsonfile = msg[1];
                vector<string> events = protocol.sendEvents(jsonfile);
                if(events.size() == 0){
                    send_mutex.unlock();
                    continue;
                }
                for ( string event : events) {
                    if(terminated){
                        break;
                    }
                    if (!connectionHandler.sendLine(event)) {
                        terminated = true;
                        receive_mutex.unlock(); // Unlock the receive mutex
                        break;
                    }
                    else{
                        receive_mutex.unlock(); // Unlock the receive mutex
                        //send_mutex.lock();
                    }
                }
                if(terminated){
                    break;
                }
                cout << "reported" << endl;
            } 
            else {
                if (!connectionHandler.sendLine(frame)) {
                    cout << "Disconnected. Exiting..." << endl;
                    terminated = true;
                    continue;
                }
            }
            if(frame == ""){
                send_mutex.unlock(); // Unlock the send mutex
                continue;
            }
            else receive_mutex.unlock(); // Unlock the receive mutex
        }
        
        socketThread.join();
        receive_mutex.unlock();
        send_mutex.unlock();
    }

    return 0;
}




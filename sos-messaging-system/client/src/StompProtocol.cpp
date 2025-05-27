#include "../include/StompProtocol.h"
#include <iostream>
#include <fstream>

vector<string>  StompProtocol::split (string s, string delimiter) {     //splitting the string line with delimiter between words into a vector.
    size_t pos_start = 0, pos_end, delim_len = delimiter.length();
    string token;
    vector<string> res;

    while ((pos_end = s.find (delimiter, pos_start)) != string::npos) {
        token = s.substr (pos_start, pos_end - pos_start);
        pos_start = pos_end + delim_len;
        res.push_back (token);
    }

    res.push_back (s.substr (pos_start));
    return res;
}



std::string trim(const std::string &str)
{
  size_t first = str.find_first_not_of(' ');
  if (first == std::string::npos)
    return "";
  size_t last = str.find_last_not_of(' ');
  return str.substr(first, (last - first + 1));
}



// Method to propagate events to all users subscribed to a channel
void StompProtocol::updateChannelEvents(const string &channel_name, const Event &new_event) {
    for (auto &user : usersEvents) {
        if (user.second.find(channel_name) != user.second.end()) {
            // Add the event to the user's channel events
            user.second[channel_name].push_back(new_event);
        }
    }
}





void StompProtocol::handleReport(const std::string &username, const std::string &channel_name, const Event &new_event) {
    // Ensure the user's data structure is initialized
    if (usersEvents.find(username) == usersEvents.end()) {
        usersEvents[username] = {};
    }
    if (usersEvents[username].find(channel_name) == usersEvents[username].end()) {
        usersEvents[username][channel_name] = {};
    }

    // Add the new event to the user's data
    usersEvents[username][channel_name].push_back(new_event);

    // Update all users subscribed to the channel
    updateChannelEvents(channel_name, new_event);
}






std::string StompProtocol::handleSummary(const std::string &username, const std::string &channel_name) {
    if (usersEvents.find(username) == usersEvents.end() || usersEvents[username].find(channel_name) == usersEvents[username].end()) {
        return "Error: No reports available for user '" + username + "' in channel '" + channel_name + "'.";
    }

    const std::vector<Event> &events = usersEvents[username][channel_name];
    if (events.empty()) {
        return "Error: No events reported for user '" + username + "' in channel '" + channel_name + "'.";
    }

    std::ostringstream summary;
    summary << "Channel: " << channel_name << "\nStats:\n";
    summary << "Total events: " << events.size() << "\n";

    int activeCount = 0, forcesArrivalCount = 0;
    for (const auto &event : events) {
        const auto &general_info = event.get_general_information();
        if (general_info.find("active") != general_info.end() && general_info.at("active") == "true") {
            activeCount++;
        }
        if (general_info.find("forces_arrival_at_scene") != general_info.end() && general_info.at("forces_arrival_at_scene") == "true") {
            forcesArrivalCount++;
        }
    }

    summary << "Active: " << activeCount << "\nForces Arrival at Scene: " << forcesArrivalCount << "\n\n";
    summary << "Event Reports:\n";

    for (const auto &event : events) {
        summary << event.get_formatted_date_time() << " - " << event.get_name() << " - " << event.get_city() << ":\n";
        summary << event.get_description() << "\n\n";
    }

    return summary.str();
}







string StompProtocol::MessageToServer(string msg){     //gets a msg from the keyboard, converts it to a proper frame(to send it to the server).
vector<string> splitmsg = split(msg," ");
string cases=splitmsg[0];
string frame="";
if(cases=="login" && splitmsg.size()!=4 ){
  cout<<"login command needs 3 args: {host:port} {username} {password}"<<endl;
  return frame;
}
else if(cases=="login" && splitmsg.size()==4){
   frame="CONNECT\naccept-version:1.2\nhost:stomp.cs.bgu.ac.il\nlogin:"+splitmsg[2]+"\npasscode:"+splitmsg[3]+"\n";
   myUser=splitmsg[2];
   host=splitmsg[1].substr(0,splitmsg[1].find(":"));
   string strport=splitmsg[1].substr(splitmsg[1].find(":")+1);
   port=stoi(strport);
   return frame;
}
/*
*/
if(cases=="join" && splitmsg.size()!=2 ){
  cout<<"join command needs 1 args: {channel_name}"<<endl;
  return frame;
}

if(cases=="join" && splitmsg.size() == 2 & splitmsg[1] == ""){
  cout<<"join command needs 1 args: {channel_name}"<<endl;
  return frame;
}

if(cases=="join" && splitmsg.size()==2)
{
    if(ChanneltoSubid.find(splitmsg[1]) != ChanneltoSubid.end()){
      cout << "already joined to channel " + splitmsg[1] << endl;
      return frame;
    }
    frame="SUBSCRIBE\ndestination:"+splitmsg[1]+"\nid:"+to_string(subId)+"\nreceipt:"+to_string(receiptId);
    receiptidtoFrame[receiptId]=frame;
    ChanneltoSubid[splitmsg[1]]=subId;
    SubidtoChannel[subId]=splitmsg[1];
    subId++;
    receiptId++;
 return frame;
}
/*
*/
if(cases=="exit" && splitmsg.size()!=2 ){
  cout<<"exit command needs 1 args: {channel_name}"<<endl;
  return frame;
}

if(cases=="exit" && splitmsg.size() == 2 & splitmsg[1] == ""){
  cout<<"exit command needs 1 args: {channel_name}"<<endl;
  return frame;
}

if(cases=="exit" && splitmsg.size()==2)
{
    if(ChanneltoSubid.find(splitmsg[1]) == ChanneltoSubid.end()){
      cout << "You are not subscribed to channel " + splitmsg[1] << endl;
      return frame;
    }
  bool isSubscribed=false;
  for(map<string,int>::iterator ii=ChanneltoSubid.begin();ii!=ChanneltoSubid.end();++ii)
  {
    if((*ii).first==splitmsg[1])
       isSubscribed=true;
  }
int id=-1;
if(isSubscribed)
  {
   id=ChanneltoSubid[splitmsg[1]];
  }
frame="UNSUBSCRIBE\nid:"+to_string(id)+"\nreceipt:"+to_string(receiptId)+"\n";
receiptidtoFrame[receiptId]=frame;
receiptId++;
return frame;
}

/*
*/
if(cases=="summary" && splitmsg.size()!=4 ){
  cout<<"summary command needs 3 args:  {channel_name} {user} {file}"<<endl;
  return frame;
}
else if (cases == "summary" && splitmsg.size()==4)
{
    string channel_name = splitmsg[1]; // Extract the channel name
    string username = splitmsg[2];    // Extract the username to summarize for
    string filePath = splitmsg[3];    // Path to the output file

    // Check if the user is subscribed to the channel
    if (ChanneltoSubid.find(channel_name) == ChanneltoSubid.end()) {
        cout << "you are not subscribed to channel " << channel_name << endl;
        return frame;
    }

    // Check if the user has reported any events
    if (usersEvents.find(username) == usersEvents.end()) {
        cout << "Error: User '" << username << "' has not reported any events." << endl;
        return "Summary failed: User not found.";
    }

    // Check if the user has reported events for the given channel
    if (usersEvents[username].find(channel_name) == usersEvents[username].end()) {
        cout << "Error: No reports available for user '" << username << "' in channel '" << channel_name << "'." << endl;
        return "Summary failed: No reports for the specified channel.";
    }

    // Fetch the events for the user and channel
    vector<Event> events = usersEvents[username][channel_name];
    if (events.empty()) {
        cout << "Error: No events reported by user '" << username << "' in channel '" << channel_name << "'." << endl;
        return "Summary failed: No events found.";
    }

    // Generate the summary
    string summary = "Channel: " + channel_name + "\nStats:\n";

    // Stats variables
    int totalReports = 0;
    int activeCount = 0;
    int forcesArrivalCount = 0;

       // Process stats and sort events
    for (const Event &event : events)
    {
        totalReports++;
        const map<string, string> &general_info = event.get_general_information();

        for (auto it = general_info.begin(); it != general_info.end(); ++it)
        {
            if (trim(it->first) == "active")
            {
                if (trim(it->second) == "true" || trim(it->second) == "1")
                {
                    //std::cout << "Condition met for value: [" << it->second << "]" << std::endl;

                    activeCount++;
                }
            }
            else
            
            if (trim(it->first) == "forces_arrival_at_scene")
            {
                if (trim(it->second) == "true" || trim(it->second) == "1")
                {
                    forcesArrivalCount++;
                }
            }
        }
    }

    // Add stats to the summary
    summary += "Total: " + to_string(totalReports) + "\n";
    summary += "Active: " + to_string(activeCount) + "\n";
    summary += "Forces Arrival at Scene: " + to_string(forcesArrivalCount) + "\n\n";

    // Sort events by date_time, then by event_name lexicographically
    sort(events.begin(), events.end(), [](const Event &a, const Event &b) {
        if (a.get_date_time() == b.get_date_time()) {
            return a.get_name() < b.get_name();
        }
        return a.get_date_time() < b.get_date_time();
    });

    // Add event reports to the summary
    summary += "Event Reports:\n\n";
    for (const Event &event : events) {
        summary += event.get_formatted_date_time() + " - " + event.get_name() + " - " + event.get_city() + ":\n";
        summary += event.get_description() + "\n\n";
    }

    // Write the summary to the specified file
    ofstream outputFile(filePath);
    if (!outputFile.is_open()) {
        cout << "Error: Unable to open file: " << filePath << endl;
        return "Summary failed: Unable to write to file.";
    }

    outputFile << summary;
    outputFile.close();

    cout << "Summary successfully written to file: " << filePath << endl;
    return "Summary written to file: " + filePath;
}


/*
*/
if (cases =="logout"){
      if(loggedIn){
    frame="DISCONNECT\nreceipt:"+to_string(receiptId)+"\n";
    receiptidtoFrame[receiptId]=frame;
    receiptId++;
      }
      else
      {
        frame="login before you do logout";
      }
      return frame;
}
if(cases == "report" && splitmsg.size()!= 2){
  cout<<"report command needs 1 args: {file}"<<endl;
  return frame;
}
else if(cases == "report" && splitmsg.size()== 2){
  return frame;
}
else{
        cout << "Illegal command, please try a different one" << endl;
    }
return frame;
}

string StompProtocol::printHash(map<string, string> msg)
{
  string frame = "";
  std::map<std::string, string>::iterator it = msg.begin();
  for (it = msg.begin(); it != msg.end(); it++)
  {
    frame = frame + it->first + ": " + it->second + " \n";
  }
  return frame;
}

vector<string> StompProtocol::sendEvents(string file)
{
    
    vector<string> myEvents;
    names_and_events eventsStruct = parseEventsFile(file);
    vector<Event> events = eventsStruct.events;
    string channel_name = eventsStruct.channel_name;
    if(ChanneltoSubid.find(channel_name) == ChanneltoSubid.end()){
      cout << "You are not registered to channel "+ channel_name << endl;
      return myEvents;
    }
    for (Event event : events)
    {
        ostringstream frame;
        frame << "SEND\n"
              << "destination:/" << channel_name << "\n\n"
              << "user:" << myUser << "\n"
              << "city:" << event.get_city() << "\n"
              << "event name:" << event.get_name() << "\n"
              << "date time:" << to_string(event.get_date_time()) << "\n"
              << "general information:\n";

        // Add general information
        const map<string, string>& general_info = event.get_general_information();
        for (const auto& [key, value] : general_info)
        {
            frame << "    " << key << ": " << value << "\n";
        }

        frame << "description:\n" << event.get_description() << "\n";
        myEvents.push_back(frame.str());
    }

    return myEvents;
}


string StompProtocol::MessageFromServer(string frame){ //gets a frame from the server, converts it to a proper msg(to prints it).
  string msg="";
  vector<string> splitframe = split(frame,"\n");
  string cases=splitframe[0];
  
  if(cases=="CONNECTED"){
    if(!loggedIn)
    {
      loggedIn=true;
      msg="Login successful";
    }
  }
  if(cases=="RECEIPT")
  { string receipt=splitframe[1].substr(splitframe[1].find(":")+1);
    int receipt_id=stoi(receipt);
    string myframe=receiptidtoFrame[receipt_id];
    vector<string> splitted = split(myframe,"\n");
    string mycase=splitted[0];
    if(mycase=="SUBSCRIBE")
    {
      string id=splitted[2].substr(splitted[2].find(":")+1);
      int sub_id=stoi(id);
      string mychannel=SubidtoChannel[sub_id];
      msg="Joined channel "+mychannel;
      currChannel=mychannel;
      vector<Event> myevents;
      usersEvents[myUser][mychannel]=myevents;
    }
    if(mycase=="UNSUBSCRIBE")
    {
      string id=splitted[1].substr(splitted[1].find(":")+1);
      int sub_id=stoi(id);
      string mychannel=SubidtoChannel[sub_id];
      SubidtoChannel.erase(sub_id);
      ChanneltoSubid.erase(mychannel);
      msg="Exited channel "+mychannel;
    }
    if(mycase=="DISCONNECT")
    {
      
      msg="Logout successful";
      loggedIn=false;
     
    }

}

if (cases == "MESSAGE")
{
    vector<string> myEvents = split(frame, "\n");
    string channel_name = myEvents[3].substr(13); // Extract channel name
    string user = (split(myEvents[5], ":"))[1];    // Extract username from the frame
    string city = (split(myEvents[6], ":"))[1];
    string event_name = (split(myEvents[7], ":"))[1];
    int date_time = stoi((split(myEvents[8], ":"))[1]);
    map<string, string> general_information;
    string description;

    // Parse general information
    int i = 9;
    if ((split(myEvents[i], ":"))[0] == "general information") {
        i++;
        while ((split(myEvents[i], ":"))[0] != "description") {
            general_information[(split(myEvents[i], ":"))[0]] = (split(myEvents[i], ":"))[1];
            i++;
        }
    }

    // Parse description
    if ((split(myEvents[i], ":"))[0] == "description") {
        description = myEvents[i + 1];
    }

    // Create and store the event
    Event event(channel_name, city, event_name, date_time, description, general_information);
    event.setEventOwnerUser(user); // Associate the event with the correct user
    // Store the event
    if (usersEvents[user][channel_name].empty()) {
        usersEvents[user][channel_name] = std::vector<Event>();
    }
    usersEvents[user][channel_name].push_back(event);
}
    if(cases=="ERROR")
    {
      msg = "ERROR\nERROR FROM THE SERVER:\n";
      //cout<< frame << endl;
      string errorname=splitframe[1].substr(9);
      if(errorname=="User already loggedin")
        msg+=errorname;
      else if(errorname=="Wrong password")
        msg+=errorname;
      // else if(errorname=="The client is already logged in , log out before trying again"){
      //   msg+=errorname;
      // }
      else if(errorname == "You are not registered to channel"){
        msg+="You are not registered to channel " + splitframe[2];
      }
      else if(splitframe[2].substr(9) == "user is not subscribed to the channel" ){
        msg+="You are not subscribed to channel ";
      }
    
    }


return msg;

}


StompProtocol::StompProtocol() : subId(0),receiptId(0),loggedIn(false), myUser(""),currChannel(""),host(""),port(0)  {

}

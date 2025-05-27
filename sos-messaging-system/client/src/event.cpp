#include "../include/event.h"
#include "../include/json.hpp"
#include <iostream>
#include <fstream>
#include <string>
#include <map>
#include <vector>
#include <sstream>
#include <cstring>
#include <iomanip>
#include <ctime>



using namespace std;
using json = nlohmann::json;

Event::Event(std::string channel_name, std::string city, std::string name, int date_time,
             std::string description, std::map<std::string, std::string> general_information)
    : channel_name(channel_name), city(city), name(name),
      date_time(date_time), description(description), general_information(general_information), eventOwnerUser("")
{
}

Event::~Event()
{
}

void Event::setEventOwnerUser(std::string setEventOwnerUser) {
    eventOwnerUser = setEventOwnerUser;
}

const std::string &Event::getEventOwnerUser() const {
    return eventOwnerUser;
}

const std::string &Event::get_channel_name() const
{
    return this->channel_name;
}

const std::string &Event::get_city() const
{
    return this->city;
}

const std::string &Event::get_name() const
{
    return this->name;
}

int Event::get_date_time() const
{
    return this->date_time;
}

const std::map<std::string, std::string> &Event::get_general_information() const
{
    return this->general_information;
}

const std::string &Event::get_description() const
{
    return this->description;
}


void split_str(const std::string& str, char delimiter, std::vector<std::string>& result)
{
    std::stringstream ss(str);
    std::string item;
    while (std::getline(ss, item, delimiter))
    {
        result.push_back(item);
    }
}




std::string Event::get_formatted_date_time() const
{
    std::time_t time = static_cast<std::time_t>(date_time);
    std::tm *tm = std::localtime(&time);
    std::ostringstream oss;
    oss << std::put_time(tm, "%d/%m/%Y %H:%M");
    return oss.str();
}



Event::Event(const std::string &frame_body): channel_name(""), city(""), 
                                             name(""), date_time(0), description(""), general_information(),
                                             eventOwnerUser("")
{
    stringstream ss(frame_body);
    string line;
    string eventDescription;
    map<string, string> general_information_from_string;
    bool inGeneralInformation = false;
    while(getline(ss,line,'\n')){
        vector<string> lineArgs;
        if(line.find(':') != string::npos) {
            split_str(line, ':', lineArgs);
            string key = lineArgs.at(0);
            string val;
            if(lineArgs.size() == 2) {
                val = lineArgs.at(1);
            }
            if(key == "user") {
                eventOwnerUser = val;
            }
            if(key == "channel name") {
                channel_name = val;
            }
            if(key == "city") {
                city = val;
            }
            else if(key == "event name") {
                name = val;
            }
            else if(key == "date time") {
                date_time = std::stoi(val);
            }
            else if(key == "general information") {
                inGeneralInformation = true;
                continue;
            }
            else if(key == "description") {
                while(getline(ss,line,'\n')) {
                    eventDescription += line + "\n";
                }
                description = eventDescription;
            }

            if(inGeneralInformation) {
                general_information_from_string[key.substr(1)] = val;
            }
        }
    }
    general_information = general_information_from_string;
}

names_and_events parseEventsFile(std::string json_path)
{
    std::ifstream f(json_path);

    if (!f.is_open())
{
    cerr << "Error: Could not open file: " << json_path << endl;
    exit(EXIT_FAILURE);
}

    json data = json::parse(f);

    std::string channel_name = data["channel_name"];

    // run over all the events and convert them to Event objects
    std::vector<Event> events;


    for (auto &event : data["events"])
    {
        std::string name = event["event_name"];
        std::string city = event["city"];
        int date_time = event["date_time"];
        std::string description = event["description"];
        std::map<std::string, std::string> general_information;
        for (auto &update : event["general_information"].items())
        {


            if (update.value().is_boolean())
            {
                // Convert boolean to string
                general_information[update.key()] = update.value() ? "true" : "false";
            }
            else if (update.value().is_string())
            {
                general_information[update.key()] = update.value();
            }
            else
            {
        // Handle other types (e.g., numbers) as strings
        general_information[update.key()] = update.value().dump();
    }
            // if (update.value().is_string())
            //     general_information[update.key()] = update.value();
            // else
            //     general_information[update.key()] = update.value().dump();
        }

        events.push_back(Event(channel_name, city, name, date_time, description, general_information));
    }
    names_and_events events_and_names{channel_name, events};

    return events_and_names;
}



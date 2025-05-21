#include <Simulation.h>
#include <Auxiliary.h>
#include <Plan.h>
#include <SelectionPolicy.h>
#include <Facility.h>
#include <Settlement.h>
#include <Action.h>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
using namespace std;

Simulation::Simulation(const string &configFilePath) : isRunning(false), planCounter(0) , actionsLog() , plans(), settlements() , facilitiesOptions(){
    // the constructor open the config file located in configFilePath
    // it will read line by line into line variable
    std::ifstream configFile(configFilePath);

    if (!configFile.is_open()){
        std::cerr << "Error: Could not open configuration file: " << configFilePath << std::endl;
        return;
    }

    std::string line;
    cout<<line<<endl;

    // getline reads until newline character
    // istringstream  allowing extraction of words and data
    // the first word in the line (type) determines the kind of object to creat
    while (getline(configFile, line)){
        std::cout << "Reading line: " << line << std::endl;  
        std::istringstream ss(line);
        std::string type;
        ss >> type;
        if (type == "settlement"){
            std::string settlementName;
            int settlementType;
            ss >> settlementName >> settlementType;
            SettlementType settlementTypeEnum = static_cast<SettlementType>(settlementType); // casting int to enum

            // Create a new Settlement object and add it to the settlements vector
            Settlement* settlementAdd = new Settlement(settlementName, settlementTypeEnum);
            Simulation::addSettlement(settlementAdd);

        }

        else if (type == "facility"){
            std::string facilityName;
            int facilityCategory;
            int facilityPrice;
            int facilityQuality;
            int facilityEco;
            int facilityEnv;

            ss >> facilityName >> facilityCategory >> facilityPrice >> facilityQuality >> facilityEco >> facilityEnv;
            FacilityCategory facilityCategoryEnum = static_cast<FacilityCategory>(facilityCategory); // casting int to enum
            FacilityType addFacility =  FacilityType(facilityName, facilityCategoryEnum, facilityPrice, facilityQuality, facilityEco, facilityEnv);
            Simulation::facilitiesOptions.push_back(addFacility);
        }

        else if (type == "plan"){
            std::string settName;
            std::string selectionP;
            ss >> settName >> selectionP;

            Settlement* settPlan;
            SelectionPolicy* policyPlan;

            // finding the settlement to mach it to the plan
            //int is a signed integer, while std::vector<T>::size() returns an unsigned integer type
            for (std::size_t i = 0; i < settlements.size(); i += 1){
                if (settlements[i]->getName() == settName){
                    settPlan = (settlements[i]);
                    break;
                }
            }

            //choose the right selection policiy
            if (selectionP == "nve"){
                policyPlan = new NaiveSelection();
            }

            else if (selectionP == "bal"){
                policyPlan = new BalancedSelection(0, 0, 0);
            }

            else if (selectionP == "eco"){
                policyPlan = new EconomySelection();
            }

            else{policyPlan = new SustainabilitySelection();}

            Simulation::addPlan(settPlan , policyPlan);
        }
    }
    configFile.close();
}



void Simulation::start(){
    cout << "The simulation has started" << endl;
    std::cout << "Entering Simulation::start() method..." << std::endl;
    Simulation::open();
    while(isRunning){
        string commandLine;
        cout << "Enter a command: ";
            getline(std::cin, commandLine); // Reading a line of input
        cout << "You entered: \"" << commandLine <<"\""<< std::endl;
        processingInput(commandLine);
    }
} 




void Simulation::addPlan(const Settlement *settlement, SelectionPolicy *selectionPolicy){
    Plan newPlan = Plan(planCounter, *(settlement), selectionPolicy, facilitiesOptions);
    plans.push_back(newPlan);
    Simulation::planCounter += 1;
}


void Simulation::addAction(BaseAction *action) {
    actionsLog.push_back(action);
}


bool Simulation::addSettlement(Settlement *settlement){
    //checking if there is other settlement with the same name
    for(std::size_t i = 0; i <settlements.size() ; i+=1){
        if(settlement -> getName() == (settlements[i] -> getName())){return false;}
        }
    settlements.push_back(settlement);
    return true;
}


bool Simulation::addFacility(FacilityType facility){
    //checking if there is other facility with the same name
    for(std::size_t i = 0; i <facilitiesOptions.size() ; i+=1){
        if(facility.getName() == facilitiesOptions[i].getName()){return false;}
        }
    facilitiesOptions.push_back(facility);
    return true;
}

bool Simulation::isSettlementExists(const string &settlementName){
    for(std::size_t i = 0; i <settlements.size() ; i+=1){
        if(settlementName == (settlements[i] -> getName())){return true;}
        }
    return false;
}


bool Simulation::isSettlementAddressExist(const Settlement* sett){
    for(std::size_t i = 0; i <settlements.size() ; i+=1){
        if(sett == settlements[i]){return true;}
        }
    return false;
}


Settlement* Simulation::getSettlement(const string &settlementName){
        for(std::size_t i =0; i < settlements.size() ; i+=1){
            if(settlements[i] -> getName() == settlementName){return settlements[i];}
        }
    return nullptr;
}

Plan& Simulation::getPlan(const int planID){
        for(std::size_t i=0 ; i<plans.size(); i+=1){
            if(plans[i].getID() == planID){return plans[i];}
        }
    throw std::runtime_error("Plan with the given ID does not exist.");
}


void Simulation::step(){
    for(Plan& p : plans){
        p.step();
    }
}


void Simulation::close(){
    this -> isRunning=false;
    for (std::size_t i =0;i < plans.size() ;i+=1){
        plans[i].printCloseStatus();
    }
}

void Simulation::open() {isRunning = true;}


bool Simulation::isFacilityExists(const string& facilityName){
    for(std::size_t i = 0; i <facilitiesOptions.size() ; i+=1){
        if(facilityName == facilitiesOptions[i].getName() ){return true;}
        }
    return false;
}


bool Simulation::isPlanExists(const int planID){
    for(std::size_t j = 0; j < plans.size(); j +=1){
        if(plans[j].getID() == planID){return true;}
    }
    return false;
}


const vector<BaseAction *> &Simulation::getActions() const {
    return actionsLog;
}



void Simulation::processingInput(std::string& userCommand){
    istringstream ss(userCommand);
    string input;
    ss >> input;
    // SimulateStep
    if (input == "step"){
        int numOfSteps;
        ss >> numOfSteps;
        SimulateStep *action = new SimulateStep(numOfSteps);
        action->act(*this);
        this->actionsLog.push_back(action);
    }

    // AddPlan
    else if (input == "plan"){
        string settName, policy;
        ss >> settName;
        ss >> policy;
        AddPlan *action = new AddPlan(settName, policy);
        action->act(*this);
        this->actionsLog.push_back(action);
    }

    // Close
    else if (input == "close"){
        Close *action = new Close();
        action->act(*this);
        this->actionsLog.push_back(action);
    }

    // AddSettlement
    else if (input == "settlement"){
        string settName;
        int settlementType;
        ss >> settName >> settlementType;
        SettlementType settlementTypeEnum = static_cast<SettlementType>(settlementType);
        AddSettlement *action = new AddSettlement(settName, settlementTypeEnum);
        action->act(*this);
        this->actionsLog.push_back(action);
    }

    // AddFacility
    else if (input == "facility"){
        string facilityName;
        int facilityCategory;
        int facilityPrice;
        int facilityQuality;
        int facilityEco;
        int facilityEnv;

        ss >> facilityName >> facilityCategory >> facilityPrice >> facilityQuality >> facilityEco >> facilityEnv;
        FacilityCategory facilityCategoryEnum = static_cast<FacilityCategory>(facilityCategory);
        AddFacility *action = new AddFacility(facilityName, facilityCategoryEnum, facilityPrice, facilityQuality, facilityEco, facilityEnv);
        action->act(*this);
        this->actionsLog.push_back(action);
    }

    // PrintPlanStatus
    else if (input == "planStatus"){
        int id;
        ss >> id;
        PrintPlanStatus *action = new PrintPlanStatus(id);
        action->act(*this);
        this->actionsLog.push_back(action);
    }

    // ChangePlanPolicy
    else if (input == "changePolicy"){
        int id;
        string newPolicy;
        ss >> id >> newPolicy;
        ChangePlanPolicy *action = new ChangePlanPolicy(id, newPolicy);
        action->act(*this);
        this->actionsLog.push_back(action);
    }

    // PrintActionsLog
    else if (input == "log"){
        PrintActionsLog *action = new PrintActionsLog();
        action->act(*this);
        this->actionsLog.push_back(action);
    }

    // BackupSimulation
    else if (input == "backup"){
        BackupSimulation *action = new BackupSimulation();
        action->act(*this);
        this->actionsLog.push_back(action);
    }

    // RestoreSimulation
    else if (input == "restore"){
        RestoreSimulation *action = new RestoreSimulation();
        action->act(*this);
        this->actionsLog.push_back(action);
    }
}



//When you assign one std::vector to another ,the standard library performs a deep copy of the vector's contents
Simulation::Simulation(const Simulation &other) : isRunning(other.isRunning),planCounter(other.planCounter),actionsLog(),
    plans(),settlements(),facilitiesOptions(){

     for (const Settlement* settlement : other.settlements) {
        settlements.push_back(settlement -> clone()); 
    }

    for (const Plan& plan : other.plans){

        Plan addPlan = Plan(plan); //copyConstructor of Plan used

        //ensure that the plan settlement a pointer belongs to settlements vector
        for(Settlement* settlement : settlements){
            
            //if statment must be true because plan is always works on some settlement
            if((addPlan.getSettlementOfPlan()).getName() == settlement -> getName()){

                //set the settlement of plan to point at same settlement in the vector
                addPlan.setSettlement(settlement);
                break;
            }
        }
        plans.push_back(addPlan);  
    }


    for (const FacilityType& facilityOption : other.facilitiesOptions) {
        facilitiesOptions.push_back(facilityOption);  // Default copy works will for FacilityType
    }

       for (const BaseAction* action : other.actionsLog) {
        actionsLog.push_back(action->clone());  
    }
}    


Simulation& Simulation::operator=(const Simulation &other){
    if(this != &other){

        /////cleaning/////
        facilitiesOptions.clear();

        for (auto action : actionsLog) {
            delete action;
        }
        actionsLog.clear();

        for (auto settlement : settlements) {
            delete settlement;
        }
        settlements.clear();
        plans.clear(); 


        /////copying/////
        isRunning = other.isRunning;
        planCounter = other.planCounter;

        for (const Plan& plan : other.plans) {
            plans.push_back(plan);   
        }

        for (const FacilityType& facilityOption : other.facilitiesOptions) {
            facilitiesOptions.push_back(facilityOption);  
        }

        for (const BaseAction* action : other.actionsLog) {
            actionsLog.push_back(action->clone());  
        }

        for (const Settlement* settlement : other.settlements) {
            settlements.push_back(settlement ->clone()); 
        }
    }
    return *this;
}


Simulation::~Simulation(){
    facilitiesOptions.clear();

   for (BaseAction* action : actionsLog) {
        delete action;
    }
    actionsLog.clear();

    for (Settlement* settlement : settlements) {
        delete settlement;
    }
    settlements.clear();
    plans.clear();
}


//need to clear plans & facilityOptions ?
Simulation &Simulation::operator=(Simulation &&other){
    if (this != &other){
        // //Free existing resources
        for (auto action : actionsLog){
            delete action;
        }
        actionsLog.clear();

        for (auto settlement : settlements){
            delete settlement;
        }
        settlements.clear();

        facilitiesOptions.clear();
        plans.clear();

        //Move resources from 'other' to 'this'
        isRunning = other.isRunning;
        planCounter = other.planCounter;

        actionsLog = std::move(other.actionsLog);
        plans = std::move(other.plans);
        settlements = std::move(other.settlements);
        facilitiesOptions = std::move(other.facilitiesOptions);
        }
    return *this;
}

Simulation::Simulation(Simulation &&other)
   :isRunning(other.isRunning) ,
    planCounter(other.planCounter),
    actionsLog(std::move(other.actionsLog)),
    plans(other.plans),
    settlements(std::move(other.settlements)),
    facilitiesOptions(other.facilitiesOptions){}










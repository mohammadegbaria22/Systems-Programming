#include <Action.h>

using namespace std;
#include <iostream>



//converts ActionStatus to String
std::string statusToString(ActionStatus status)
{
    
    switch (status) {
        case ActionStatus::COMPLETED:
            return "COMPLETED";
        case ActionStatus::ERROR:
            return "ERROR";
        default:
            return "UNKNOWN"; 
    }
}


//BaseAction
BaseAction::BaseAction():errorMsg(""),status(ActionStatus::ERROR){}

ActionStatus BaseAction::getStatus() const{
    return this->status;
}

void BaseAction::complete(){
    this->status=ActionStatus::COMPLETED;
}

void BaseAction::error(string errorMsg){
    this->status = ActionStatus::ERROR;
    this-> errorMsg = errorMsg;

    //should use the getErrorMsg because it's private
    cout<<"Error: "<<this->getErrorMsg()<<endl;
}

const string &BaseAction::getErrorMsg() const{
    return this->errorMsg;
}


//SimulationStep
SimulateStep::SimulateStep(const int numOfSteps):BaseAction(),numOfSteps(numOfSteps){}

void SimulateStep::act(Simulation &simulation) {
    for (int i = 0; i < this -> numOfSteps; i+=1){
        simulation.step();
    }
    this->complete();
}


const string SimulateStep::toString() const{
    return "step "+to_string(this->numOfSteps)+" "+statusToString(getStatus());
}



SimulateStep *SimulateStep::clone() const{
    return new SimulateStep(*this); 
}


//AddPlan
AddPlan::AddPlan(const string &settlementName, const string &selectionPolicy):BaseAction()
,settlementName(settlementName),selectionPolicy(selectionPolicy){}



void AddPlan::act(Simulation &simulation){

    //settlement doesn't exist
    if(simulation.isSettlementExists(settlementName)==false){
        error("Cannot create this plan!"); 
    }

    else if(this->selectionPolicy=="nve"){
        Settlement* settlement =  simulation.getSettlement(this->settlementName);
        NaiveSelection* nve = new NaiveSelection();
        simulation.addPlan(settlement , nve);
        complete();
    }

    else if(this -> selectionPolicy== "bal"){
        Settlement* settlement =  simulation.getSettlement(this->settlementName);
        BalancedSelection* balanced = new BalancedSelection(0,0,0);
        simulation.addPlan(settlement , balanced);
        complete();
    }

    else if(this->selectionPolicy=="eco"){
        Settlement* settlement =  simulation.getSettlement(this->settlementName);
        EconomySelection* eco = new EconomySelection();
        simulation.addPlan(settlement , eco);
    }


    else if(this->selectionPolicy=="env"){
        Settlement* settlement =  simulation.getSettlement(this->settlementName);
        SustainabilitySelection* env = new SustainabilitySelection();
        simulation.addPlan(settlement , env);
        complete();
    }

    //SelectionPolicy doesn't exist
    else {error("Cannot create this plan!");}
}



const string AddPlan::toString() const{
    return "plan " + settlementName + " " +statusToString(getStatus());
}

AddPlan *AddPlan::clone() const{
    return new AddPlan(*this);
}


//AddSettlement
AddSettlement::AddSettlement(const string &settlementName,SettlementType settlementType) : BaseAction() 
,settlementName(settlementName)
,settlementType(settlementType){}

void AddSettlement::act(Simulation &simulation){
    if(simulation.isSettlementExists(settlementName)){
        error("settlement already exists!");}

    //create a new settlement and add it to settlements
    Settlement* settAddAction = new Settlement(settlementName,settlementType);
    simulation.addSettlement(settAddAction);
    complete();
}


AddSettlement *AddSettlement::clone() const {
    return new AddSettlement(*this);
}

const string AddSettlement::toString() const {
    return "settlement " + settlementName+" "+statusToString(getStatus());
}


//AddFacility
 AddFacility::AddFacility(const string &facilityName, const FacilityCategory facilityCategory, const int price, const int lifeQualityScore, const int economyScore, const int environmentScore) : BaseAction()
,facilityName(facilityName) 
,facilityCategory(facilityCategory) 
,price(price)
,lifeQualityScore(lifeQualityScore)
, economyScore(economyScore)
, environmentScore(environmentScore){}

void AddFacility::act(Simulation &simulation){
    //facility already exists
    if(simulation.isFacilityExists(facilityName)){
        return  error("Cannot create this facility!");}

    //if don't creat a new facility and add it to facilityOptions
    FacilityType facilityAddAction = FacilityType(facilityName,facilityCategory,price,lifeQualityScore,economyScore,environmentScore);

    simulation.addFacility(facilityAddAction);
    complete();
}

AddFacility *AddFacility::clone() const {
    return new AddFacility(*this);
}

const string AddFacility::toString() const {
    int category=static_cast<int>(facilityCategory);
    return "facility " + facilityName+" "+to_string(category)+" "+to_string(price)+" "+to_string(lifeQualityScore)+" "+to_string(economyScore)+" "+to_string(environmentScore)+" "+statusToString(getStatus());
}


//PrintPlanStatus
 PrintPlanStatus::PrintPlanStatus(int planID) : BaseAction()
,planId(planID){}

void PrintPlanStatus::act(Simulation &simulation){

    if(simulation.isPlanExists(planId)){
    Plan actionPlan = simulation.getPlan(planId);
    actionPlan.printStatus();
    complete();}

    else{error("plan does not exist");}
}


PrintPlanStatus *PrintPlanStatus::clone() const{
    return new PrintPlanStatus(*this);
}

const string PrintPlanStatus::toString() const{
    return "planStatus "+to_string(planId)+" "+statusToString(getStatus());
}


//ChangePlanPolicy
ChangePlanPolicy::ChangePlanPolicy(const int planId, const string &newPolicy) : BaseAction()
, planId(planId)
,newPolicy(newPolicy){}

void ChangePlanPolicy::act(Simulation &simulation){

        if(!(simulation.isPlanExists(planId))) {return error("plan do not exist");}

        //need to be ref - else we change the policy in the copied plan!
        Plan& actionPlan = simulation.getPlan(planId);
        const SelectionPolicy* oldPolicy = actionPlan.getSelectionPolicy();


        const string oldPolicyName = oldPolicy -> toString();
        if(oldPolicyName == newPolicy){return error ("this is the same policy");}

        cout<<"planID: " <<planId << endl; 
        cout<<"PreviousPolicy: " << oldPolicyName<<endl;

        if(newPolicy == "nve"){
            NaiveSelection* nPolicy = new NaiveSelection();
            actionPlan.setSelectionPolicy(nPolicy); 
        }

        else if(newPolicy == "bal"){
            BalancedSelection* bPolicy = new BalancedSelection(0,0,0); 
            actionPlan.setSelectionPolicy(bPolicy);
        }

        else if(newPolicy == "eco"){
            EconomySelection* ecoPolicy = new EconomySelection();
            actionPlan.setSelectionPolicy(ecoPolicy);
        }

        else if(newPolicy == "env"){
            SustainabilitySelection* envPolicy = new SustainabilitySelection();
            actionPlan.setSelectionPolicy(envPolicy);
        }

    cout<<"newPolicy: " << newPolicy << endl;
    complete();
    }


ChangePlanPolicy *ChangePlanPolicy::clone() const{
    return new ChangePlanPolicy(*this);
}

const string ChangePlanPolicy::toString() const{
    return "changePolicy "+to_string(planId)+" "+newPolicy+" "+statusToString(getStatus());
}



//PrintActionLogs
PrintActionsLog::PrintActionsLog() : BaseAction() {}

void PrintActionsLog::act(Simulation &simulation){

    vector<BaseAction*> actionLogs = simulation.getActions() ;
    int numOfActions = actionLogs.size() ;
    for (int i = 0; i < numOfActions; i+=1) {
        std::cout << actionLogs[i] -> toString() <<std::endl ;
    }
    complete();
}


PrintActionsLog *PrintActionsLog::clone() const {
    return new PrintActionsLog(*this);
}

const string PrintActionsLog::toString() const {
    return "log COMPLETED" ;
}


//Close
Close::Close():BaseAction(){}

void Close::act(Simulation &simulation){
    simulation.close();
    complete();
}

Close *Close::clone() const{
    return new Close(*this);
}

const string Close::toString() const{
    return "close COMPLETED";
}


//BackupSimulation
BackupSimulation::BackupSimulation(){}

void BackupSimulation::act(Simulation &simulation){

    if(backup == nullptr){
        backup = new Simulation(simulation);
    }
    else{
        delete backup;
        backup = new Simulation(simulation);  
}

     complete();

}

BackupSimulation *BackupSimulation::clone() const{
        return new BackupSimulation(*this);
}


const string BackupSimulation::toString() const{
    return "BackUp COMPLETED";
}



RestoreSimulation::RestoreSimulation(){}

void RestoreSimulation::act(Simulation &simulation){
    if(backup!=nullptr){
        
        simulation.operator=(*backup);
        complete();
    }
    else{
        error("No backup available");
    }
}



RestoreSimulation *RestoreSimulation::clone() const{
        return new RestoreSimulation(*this);
}

const string RestoreSimulation::toString() const{
   return "restore "+statusToString(getStatus());
}



#include <Plan.h>
using namespace std;
#include <iostream>

Plan::Plan(const int planId, const Settlement &settlement, SelectionPolicy *selectionPolicy, const vector<FacilityType> &facilityOptions):
plan_id(planId),settlement(&settlement),selectionPolicy(selectionPolicy),status(PlanStatus::AVALIABLE) , facilities()
, underConstruction() ,facilityOptions(facilityOptions),life_quality_score(0),economy_score(0),environment_score(0){}

const int Plan::getlifeQualityScore() const{return life_quality_score;}

const int Plan::getEconomyScore() const {return economy_score;}

const int Plan::getEnvironmentScore() const{return environment_score;}

void Plan::setSelectionPolicy(SelectionPolicy* newSelectionPolicy){
    //ensure no memoryleak
    delete this -> selectionPolicy;
    this -> selectionPolicy = newSelectionPolicy;}


void Plan::step(){
    //1 & 2//
    SettlementType planLimitType = (this  -> getSettlementOfPlan()).getType();
    size_t planLimit = static_cast<int>(planLimitType) + 1;

    if(this->getStatus() == PlanStatus::AVALIABLE){

        while(this -> underConstruction.size() < planLimit){
            FacilityType selectedFacility = this -> selectionPolicy ->selectFacility(facilityOptions);
            Facility* addedFacility = new Facility(selectedFacility , this -> settlement -> getName() ); //selectedFacility is ref& ?!
            this -> underConstruction.push_back(addedFacility);
        }
    }    

        //3//
        for (size_t i = 0; i < underConstruction.size(); ) {
            Facility* facilityPoint = underConstruction[i]; // Get the current facility pointer
            facilityPoint -> step();
            if (facilityPoint->getStatus() == FacilityStatus::OPERATIONAL) {

                //facilities.push_back(facilityPoint);
                addFacility(facilityPoint);
                underConstruction.erase(underConstruction.begin() + i);// Remove from vector
            }
            else {i += 1;} // Move to the next element only if no element is erased
        }

        //4//
        if(underConstruction.size() == planLimit){this -> setPlanStatus(PlanStatus::BUSY);}
        else{this -> setPlanStatus(PlanStatus::AVALIABLE);}
 }


void Plan::printStatus(){
    cout<<"PlanID: "<<this->plan_id<<endl;
    cout<<"SettlementName " <<settlement->getName()<<endl;

    if (status==PlanStatus::AVALIABLE){cout<<"PlanStatus: AVAILABLE"<<endl;}
    else {cout<<"PlanStatus: BUSY"<<endl;}

    cout<<"SelectionPolicy: "<<(this -> selectionPolicy) -> toString()<<endl;
    cout<<"LifeQualityScore: "<<life_quality_score<<endl;
    cout<<"EconomyScore: "<<economy_score<<endl;
    cout<<"EnvrionmentScore: "<<environment_score<<endl;

    for (size_t i = 0; i < this->getUnderConstructions().size(); i++){
        cout<<"FacilityName: "<<this->getUnderConstructions()[i]->getName()<<endl;
        cout<<"FacilityStatus: UNDER_CONSTRUCTIONS"<<endl;
    }
    
    for (size_t i = 0; i <this->getFacilities().size(); i++){
        cout<<"FacilityName: "<<this->getFacilities()[i]->getName()<<endl;
        cout<<"FacilityStatus: OPERATIONAL"<<endl;    
    }
}


void Plan::printCloseStatus(){
    cout<<"PlanID: "<<this->plan_id<<endl;
    cout<<"SettlementName " <<settlement->getName()<<endl;

    if (status==PlanStatus::AVALIABLE){cout<<"PlanStatus: AVAILABLE"<<endl;}
    else {cout<<"PlanStatus: BUSY"<<endl;}

    cout<<"SelectionPolicy: "<<(this -> selectionPolicy) -> toString()<<endl;
    cout<<"LifeQualityScore: "<<life_quality_score<<endl;
    cout<<"EconomyScore: "<<economy_score<<endl;
    cout<<"EnvrionmentScore: "<<environment_score<<endl;
}


const vector<Facility*>& Plan::getFacilities() const {
    return this -> facilities;
}


void Plan::addFacility(Facility* Facility){
    
    if(Facility->getStatus()==FacilityStatus::OPERATIONAL){

        this -> life_quality_score += Facility -> getLifeQualityScore();
        this -> economy_score += Facility -> getEconomyScore();
        this -> environment_score += Facility -> getEnvironmentScore();
        this -> facilities.push_back(Facility);
    }
    else underConstruction.push_back(Facility);
}



const string Plan::toString() const{
    return "plan is craeted";
    }


const int Plan::getID() const{
    return plan_id;
}


const vector<Facility *> &Plan::getUnderConstructions() const{
    return underConstruction;
}


const SelectionPolicy* Plan::getSelectionPolicy() const {
    return selectionPolicy;
}


void Plan::setPlanStatus(PlanStatus newStatus){
    status=newStatus;
}


const Settlement &Plan::getSettlementOfPlan() const{
    return *settlement;
}

const Settlement* Plan::getSettlementAddress() const{
    return settlement;
}

void Plan::setSettlementNull(){
    settlement = nullptr;
}

void Plan::setSettlement(Settlement* sett){
    settlement = sett;
}


const PlanStatus Plan::getStatus() const{
 return this->status;
}

 // Copy Constructor
    Plan::Plan(const Plan& other)
        : plan_id(other.plan_id),
          settlement(other.settlement), // Copying the pointer , NOT owning it
          selectionPolicy(),
          status(other.status),
          facilities(),
          underConstruction(),
          facilityOptions(other.facilityOptions),
          life_quality_score(other.life_quality_score),
          economy_score(other.economy_score),
          environment_score(other.environment_score)
        {
        selectionPolicy = other.selectionPolicy -> clone();    

        for (Facility* facility : other.facilities) {
            facilities.push_back(new Facility(*facility));  
        }

        for (Facility *facility : other.underConstruction) {
            underConstruction.push_back(new Facility(*facility)); 
        }
    }



    Plan::~Plan() {
        // Clean up dynamically allocated memory
        delete selectionPolicy;

        for (Facility *facility : facilities) {
            delete facility;
        }
        facilities.clear();

        for (Facility *facility : underConstruction) {
            delete facility;
        }
        underConstruction.clear();  
    }


    Plan::Plan(Plan &&other)
    // (v1._data) to v2, so now v2._data points to the same memory block of v1.
    //Since facilityOptions is only a reference, no data is transferred or duplicated. Both the new object and the moved-from 
    //object will still reference the same std::vector<FacilityType>.
    : plan_id(other.plan_id) ,
    settlement(other.settlement),          
    selectionPolicy(other.selectionPolicy),
    status(other.status),
    facilities(std::move(other.facilities)) ,
    underConstruction(std::move(other.underConstruction)),
    facilityOptions(other.facilityOptions), 
    life_quality_score(other.life_quality_score) ,
    economy_score(other.economy_score),
    environment_score(other.environment_score)
    {
    other.facilities.clear();
    other.underConstruction.clear();
    other.settlement = nullptr;
    other.selectionPolicy = nullptr;
}



 























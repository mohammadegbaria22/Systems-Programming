#pragma once
#include <vector>
#include "Facility.h"
#include "Settlement.h"
#include "SelectionPolicy.h"
//#include "Simulation.h"
using std::vector;

enum class PlanStatus {
    AVALIABLE,
    BUSY,
};

class Plan {
    public:
        Plan(const int planId, const Settlement &settlement, SelectionPolicy *selectionPolicy, const vector<FacilityType> &facilityOptions);
        const int getlifeQualityScore() const;
        const int getEconomyScore() const;
        const int getEnvironmentScore() const;
        void setSelectionPolicy(SelectionPolicy *selectionPolicy);
        void step();
        void printStatus();
        void printCloseStatus();
        const vector<Facility*> &getFacilities() const;
        void addFacility(Facility* facility);
        const string toString() const;

        //added methods:
        const int getID() const;
        const vector<Facility*> &getUnderConstructions() const;
        const SelectionPolicy* getSelectionPolicy() const; 
        void setPlanStatus(PlanStatus newStatus);
        const Settlement& getSettlementOfPlan() const;
        const Settlement* getSettlementAddress() const;
        void setSettlementNull();
        void setSettlement(Settlement* sett);
        const PlanStatus getStatus() const;
        
        //rule of 5 
        Plan(const Plan& other);
        ~Plan();
        Plan& operator=(const Plan& other) = delete; 
        Plan(Plan &&other); 
        Plan& operator=(const Plan&& other) = delete; 



    private:
        int plan_id;
        const Settlement* settlement;
        SelectionPolicy* selectionPolicy; //What happens if we change this to a reference?
        PlanStatus status;
        vector<Facility*> facilities;
        vector<Facility*> underConstruction;
        const vector<FacilityType> &facilityOptions;
        int life_quality_score, economy_score, environment_score;

        
};


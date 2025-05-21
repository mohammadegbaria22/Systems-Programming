#include <Facility.h>
using namespace std;

FacilityType::FacilityType(const string &name, const FacilityCategory category, const int price, const int lifeQuality_score, const int economy_score, const int environment_score):
name(name),category(category),price(price),lifeQuality_score(lifeQuality_score),economy_score(economy_score),environment_score(environment_score){}


const string &FacilityType::getName() const{
    return name;}


int FacilityType::getCost() const{
    return price;}


int FacilityType::getLifeQualityScore() const{
    return lifeQuality_score;}


int FacilityType::getEnvironmentScore() const{
    return environment_score;}


int FacilityType::getEconomyScore() const{
    return economy_score;}


FacilityCategory FacilityType::getCategory() const{
    return category;}



Facility::Facility(const string &name, const string &settlementName, const FacilityCategory category, const int price, const int lifeQuality_score, const int economy_score, const int environment_score):
FacilityType(name,category,price,lifeQuality_score,economy_score,environment_score),settlementName(settlementName),status(FacilityStatus::UNDER_CONSTRUCTIONS),timeLeft(price){}
Facility::Facility(const FacilityType &type, const string &settlementName):FacilityType(type), settlementName(settlementName),status(FacilityStatus::UNDER_CONSTRUCTIONS),timeLeft(this->price){}


const string &Facility::getSettlementName() const{
    return settlementName;
    }


const int Facility::getTimeLeft() const{
    return timeLeft;
    }


FacilityStatus Facility::step(){
    this -> timeLeft -= 1;
    if(this->timeLeft==0){
        this->setStatus(FacilityStatus::OPERATIONAL);
    }
    return this->status;
}    


void Facility::setStatus(FacilityStatus newstatus){
    Facility::status=newstatus;
}


const FacilityStatus& Facility::getStatus() const{
    return status;
}



const string Facility::toString() const{
    return name +" "+ facilityCategoryToString(category)+" "+to_string(price) +" "+ to_string(lifeQuality_score) +" "+ to_string(economy_score)  +" "+ to_string(environment_score);
}


const string Facility::facilityCategoryToString(FacilityCategory type)const{
    if (type == FacilityCategory::LIFE_QUALITY){return "0";}
    else if (type == FacilityCategory:: ECONOMY){return "1";}
    else {return "2";}
}


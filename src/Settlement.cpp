#include "Settlement.h"
using namespace std;

Settlement::Settlement(const string &name, SettlementType type) : name(name) , type(type) {}

const string& Settlement::getName() const{return Settlement::name;}
SettlementType Settlement::getType() const{return Settlement::type;}
const string Settlement::toString() const {
        return "the settlement name is: " + name  +" of type: " + settlementTypeToString(type);
}

const string Settlement::settlementTypeToString(SettlementType type)const{
    if (type == SettlementType::VILLAGE){return "0";}
    else if (type == SettlementType:: CITY){return "1";}
    else {return "2";}
}

Settlement* Settlement::clone() const{
    return new Settlement(*this);
}


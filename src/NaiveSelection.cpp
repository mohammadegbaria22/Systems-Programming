#include <SelectionPolicy.h>
#include <iostream>
#include <vector>
#include <string>

//check if -1 actualy holds the last policy selected when changes to new policy
NaiveSelection::NaiveSelection() : lastSelectedIndex (-1){}

const FacilityType& NaiveSelection::selectFacility(const vector<FacilityType>& facilitiesOptions){

    int fSize = facilitiesOptions.size();
    if (fSize == 0){throw std::runtime_error("No facilities available for selection.");}


    //make sure to build more facilites than the provided number if needed
    for(int j = 0 ; j < fSize ; j +=1 ){
        lastSelectedIndex = (lastSelectedIndex+1+j)%fSize;
        return facilitiesOptions[lastSelectedIndex];
    }
     return facilitiesOptions[lastSelectedIndex];
}

const string NaiveSelection::toString() const{return "nve";}

NaiveSelection* NaiveSelection::clone() const {
    return new NaiveSelection(*this); 
}



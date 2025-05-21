#include <SelectionPolicy.h>
#include <iostream>
#include <vector>
#include <string>



SustainabilitySelection::SustainabilitySelection() : lastSelectedIndex(-1){}


const FacilityType& SustainabilitySelection::selectFacility(const vector<FacilityType>& facilitiesOptions){
    int fSize = facilitiesOptions.size();
    if (fSize == 0){throw std::runtime_error("No facilities available for selection.");}


   
    for(int j = 0; j < fSize ; j +=1 ){

        FacilityCategory checkCategory = facilitiesOptions[(lastSelectedIndex+1+j)%fSize].getCategory();
        if( checkCategory == FacilityCategory::ENVIRONMENT){
            lastSelectedIndex = (lastSelectedIndex+1+j)%fSize;
            return facilitiesOptions[lastSelectedIndex];
        }
    }
    lastSelectedIndex++;
    return facilitiesOptions[(lastSelectedIndex)%fSize];
}


const string SustainabilitySelection::toString() const{return "env";}

SustainabilitySelection* SustainabilitySelection::clone() const {
    return new SustainabilitySelection(*this); 
}




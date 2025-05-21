all: clean compile link run

compile:
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/main.o src/main.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Settlement.o src/Settlement.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Facility.o src/Facility.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/BalancedSelection.o src/BalancedSelection.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/NaiveSelection.o src/NaiveSelection.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/SustainabilitySelection.o src/SustainabilitySelection.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/EconomySelection.o src/EconomySelection.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Auxiliary.o src/Auxiliary.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Simulation.o src/Simulation.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Plan.o src/Plan.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/Action.o src/Action.cpp
	
	
	
link:
	g++  -o  bin/main bin/Action.o bin/Plan.o bin/Simulation.o bin/Auxiliary.o bin/EconomySelection.o bin/BalancedSelection.o bin/NaiveSelection.o bin/SustainabilitySelection.o bin/Facility.o bin/Settlement.o bin/main.o
clean:
	rm -f bin/*.o

run:
	rm -f bin/*.o
	./bin/main
	
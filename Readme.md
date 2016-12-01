## Railway_Subway_Metro_System

Implements a real life railway / subway / metro system

## Entities

Person, Train, Station and line

## Person

Person (threads) are assigned random start and destination stations. Person enters station accordingly and waits for train to arrive. 
When train arrives person checks if he can enter train based on his destination and train capacity and then decides to enter train or not.
Once in train, the person waits for train announcement and exits when the train reaches his destination.

## Train

Train (threads) run to and fro between stations in their respective lines multiple times. Once train reaches station it announces for persons in train that the station has arrived. Before entering station checks if there is vacancy available in station. If not available then waits till another train moves out.

## Station

Station lets person know that train has arrived. Station tells train whether vacancy is available.

## Line

Line contains multiple stations. Train goes to the stations in the order defined by the line. Lines can be used to represent slow / express trains or different routes altogether.

## Notes

Currently in the input files I have used stations from Western line (Mumbai Suburban Railway) and Central line (Mumbai Suburban Railway).
But this code can be easily extended to NYC MTA System or BART system in SFO

Wiki :
https://en.wikipedia.org/wiki/Western_line_(Mumbai_Suburban_Railway)
https://en.wikipedia.org/wiki/Central_line_(Mumbai_Suburban_Railway)

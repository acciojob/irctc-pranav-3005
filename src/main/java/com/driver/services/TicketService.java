package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train= trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        Passenger bookingPassenger= passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();

        String[] trainRoute= train.getRoute().split(",");
        boolean from=false;
        boolean to=false;

        int fromIndex=0;
        int toindex=0;
        for(int i=0;i<trainRoute.length;i++)
        {
            if( bookTicketEntryDto.getFromStation().toString().equals( trainRoute[i] ) )
            {
                fromIndex=i;
                from=true;
            }
            else if ( bookTicketEntryDto.getToStation().toString().equals( trainRoute[i] ) )
            {
                toindex=i;
                to=true;
            }
        }

        if(from && to);
        else
            throw new Exception("Invalid stations");

        //calculating available tickets btwn from and to stations
        List<String> routeList=new ArrayList<>();
        for(int i=0;i<trainRoute.length;i++)
            routeList.add(trainRoute[i]);

        int count=train.getNoOfSeats();

        String currentPassengerFromCity=bookTicketEntryDto.getFromStation().toString();
        String currentPassengerToCity=bookTicketEntryDto.getToStation().toString();

        for(Ticket ticket:train.getBookedTickets())
        {
            String bookedPassengerFromCity=ticket.getFromStation().toString();
            String bookedPassengerToCity=ticket.getToStation().toString();

            count-=ticket.getPassengersList().size();  //calculates (total seats - booked seats)

            if( routeList.indexOf(bookedPassengerToCity) <= routeList.indexOf(currentPassengerFromCity) )
            {
                count+=ticket.getPassengersList().size(); //booked passengers not in our journey
            }
            else if( routeList.indexOf(bookedPassengerFromCity) >= routeList.indexOf(currentPassengerToCity) )
            {
                count+=ticket.getPassengersList().size();  //booked passengers not in our journey
            }
        }

        if(bookTicketEntryDto.getPassengerIds().size() > count)
            throw new Exception("Less tickets are available");

        //exceptions over..........
        int noOfStationsToCover= toindex-fromIndex;
        int costPerStation= 300;

        Ticket ticket=new Ticket();

        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(noOfStationsToCover * costPerStation);
        ticket.setTrain(train);
        //ticket.getPassengersList().add(bookingPassenger);

        // ticket - passenger
        Ticket savedTicket=ticketRepository.save(ticket);

        bookingPassenger.getBookedTickets().add(savedTicket);
        Passenger savedPassenger=passengerRepository.save(bookingPassenger);

        savedTicket.getPassengersList().add(savedPassenger);
        Ticket savedTicket1=ticketRepository.save(savedTicket);

        // train - ticket
        train.getBookedTickets().add(savedTicket1);
        trainRepository.save(train);




       return savedTicket1.getTicketId();

    }
}

package edu.bu.met.cs665.dispatchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import edu.bu.met.cs665.customers.Customer;
import edu.bu.met.cs665.orders.Order;
import edu.bu.met.cs665.stores.Store;
import edu.bu.met.cs665.vehicles.Vehicle;

/**
 * Creates a StoreDispatcher singleton for all classes to communicate with
 * @author Richard Hanks
 *
 */
public class StoreDispatcher implements Dispatcher {
  List<Vehicle> registeredVehicles = new ArrayList<>();
  List<Store> registeredStores = new ArrayList<>();
  List<Order> ordersNotScheduled = new ArrayList<>();
  List<Order> ordersInTransit = new ArrayList<>();
  List<Order> ordersDeliveryComplete = new ArrayList<>();
  
  private static  StoreDispatcher storeDispatcherInstance;
  
  static{
    storeDispatcherInstance = new StoreDispatcher();
    
  }


  /**
   * Method for getting the singleton StoreDispatcher instance.
   * @return The singleton StoreDispatcher instance
   */
  public static StoreDispatcher getInstance(){
    return storeDispatcherInstance;
  }
  
  public List<Order> getOrdersNotScheduled() {
    return ordersNotScheduled;
  }

  public List<Order> getOrdersInTransit() {
    return ordersInTransit;
  }

  public List<Order> getOrdersDeliveryComplete() {
    return ordersDeliveryComplete;
  }

  /**
   * Method for registering a vehicle with the dispatcher.  Upon successful
   * registration with the dispatcher, a map of is returned that assigns the 
   * vehicle to a distance from each store.
   */
  @Override
  public Map<Store, Integer> registerVehicle(Vehicle vehicle) {
    // Only register the vehicle if it isn't already registered
    if(!registeredVehicles.contains(vehicle)){
      registeredVehicles.add(vehicle);
    }
    return makeRandomInitialDistanceAssignments();
  }
  
  /**
   * Method for dispatching vehicles to deliver all of the packages
   * in the list of orders to be delivered.
   * @postcondition The ordersNotScheduled List has been emptied and the 
   * orders have been "delivered" to their customer destinations
   */
  public void dispatchVehicles(){
    // While there are still orders that have not been scheduled for delivery
    Vehicle deliveryVehicle = null;
    int currentSmallestTotalDistance = 0;
    while(this.getOrdersNotScheduled().size() > 0){
      for(Order order: this.getOrdersNotScheduled()){
        // Find the vehicle that is marked as available for deliveries and has the smallest total distance
        for(Vehicle vehicle: this.registeredVehicles){
          // If vehicle is available for deliveries
          int totalVehicleDistance = StoreDispatcher.getTotalDistance(order.getStore(), order.getCustomer(), vehicle);
          if(vehicle.getStatus()){
            // If this is the first time through the vehicle for loop, set deliveryVehicle to the first instance
            if (deliveryVehicle == null){
              deliveryVehicle = vehicle;
              // Current smallest total distance will be the first first vehicle we check by default.
              currentSmallestTotalDistance = totalVehicleDistance;
            }else if(totalVehicleDistance < totalVehicleDistance){
              deliveryVehicle = vehicle;
              currentSmallestTotalDistance = totalVehicleDistance;
            }
            // If a delivery vehicle has been found, assign it to deliver this order
            if(deliveryVehicle != null){
              // Assign order to vehicle
              deliveryVehicle.deliverOrder(order, currentSmallestTotalDistance);
              // Move order to list of scheduled orders
              // Set deliveryVehicle back to null to begin the process check again
            }
          }
        }
      }
    }
  }
  
  /**
   * Given a store and customer instance, computes the total distance between the vehicle and the store
   * and the customer and the store.  This is the rough approximation of how far the vehicle will need to travel.
   * @param store The store from where the order will originate for delivery
   * @param customer The customer to whome the package will be delivered
   * @parm the vehicle which will deliver the order
   * @return the total distance the vehicle will travel to complete this delivery.
   */
  public static int getTotalDistance(Store store, Customer customer, Vehicle vehicle){
    System.out.println("StoreDispatcher distanceFromEachStore: " + customer.getDistanceFromEachStore());
//    customer.getDistanceFromEachStore().get(customer.getDistanceFromEachStore().keySet().);
    Store storeFromKeySet = customer.getDistanceFromEachStore().keySet().iterator().next();
    System.out.println("store: " + store);
    System.out.println("key set store: " + storeFromKeySet);
    System.out.println("==" + (store == storeFromKeySet));
    System.out.println(customer.getDistanceFromEachStore().containsKey(store));
    int customerToStoreDistance = customer.getDistanceFromEachStore().get(store);
    int vehicleToStoreDistance = vehicle.getDistancesFromEachStore().get(vehicle);
    return customerToStoreDistance + vehicleToStoreDistance;
  }
  
//  @Override
//  public int hashCode() {
//    final int prime = 31;
//    int result = 1;
//    result =
//        prime * result + ((ordersDeliveryComplete == null) ? 0 : ordersDeliveryComplete.hashCode());
//    result = prime * result + ((ordersInTransit == null) ? 0 : ordersInTransit.hashCode());
//    result = prime * result + ((ordersNotScheduled == null) ? 0 : ordersNotScheduled.hashCode());
//    result = prime * result + ((registeredStores == null) ? 0 : registeredStores.hashCode());
//    result = prime * result + ((registeredVehicles == null) ? 0 : registeredVehicles.hashCode());
//    return result;
//  }
//
//  @Override
//  public boolean equals(Object obj) {
//    if (this == obj)
//      return true;
//    if (obj == null)
//      return false;
//    if (getClass() != obj.getClass())
//      return false;
//    StoreDispatcher other = (StoreDispatcher) obj;
//    if (ordersDeliveryComplete == null) {
//      if (other.ordersDeliveryComplete != null)
//        return false;
//    } else if (!ordersDeliveryComplete.equals(other.ordersDeliveryComplete))
//      return false;
//    if (ordersInTransit == null) {
//      if (other.ordersInTransit != null)
//        return false;
//    } else if (!ordersInTransit.equals(other.ordersInTransit))
//      return false;
//    if (ordersNotScheduled == null) {
//      if (other.ordersNotScheduled != null)
//        return false;
//    } else if (!ordersNotScheduled.equals(other.ordersNotScheduled))
//      return false;
//    if (registeredStores == null) {
//      if (other.registeredStores != null)
//        return false;
//    } else if (!registeredStores.equals(other.registeredStores))
//      return false;
//    if (registeredVehicles == null) {
//      if (other.registeredVehicles != null)
//        return false;
//    } else if (!registeredVehicles.equals(other.registeredVehicles))
//      return false;
//    return true;
//  }

  /**
   * 
   * @return A map where the keys are all of the registered stores and the
   * values are randomly assigned Integer values with indicates the assigned
   * distance in arbitrary units from the the given Store key.
   */
  public Map<Store, Integer> makeRandomInitialDistanceAssignments(){
    Map<Store, Integer> assignmentMapping = new HashMap<>();
    int random;
    for(Store store: this.registeredStores){
//      System.out.println("Store is: " + store);
      random = new Random().nextInt(50);
      assignmentMapping.put(store, random);
    }
//    System.out.println("assignmentMapping is: " + assignmentMapping);
    return assignmentMapping;
  }

  @Override
  public void removeVehicle(Vehicle vehicle) { 
      // Only try removing a vehicle from the list if it is in the list
      if(this.registeredVehicles.contains(vehicle)){
        this.registeredVehicles.remove(this.registeredVehicles.indexOf(vehicle));
      }
  }

  @Override
  public void dispatchVehicle(Vehicle vehicle, Order order) {
    // TODO Auto-generated method stub

  }

  /**
   * Receives a new order from a store and adds it to the list
   * of orders that have not been scheduled.
   * @postcondition The given order has been added to the OrdersNotScheduled List
   * @param order the order to be added to the list of orders not scheduled
   */
  @Override
  public void receiveOrder(Order order) {
      if(!ordersNotScheduled.contains(order)){
        ordersNotScheduled.add(order);
      }
  }

  @Override
  public void updateVehicleDistance(Vehicle vehicle, int distance) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<Vehicle> getRegisteredVehicles() {
    return this.registeredVehicles;
  }

  @Override
  public void registerStore(Store store) {
    if(!this.registeredStores.contains(store)){
      this.registeredStores.add(store);
    }
  }

  @Override
  public void removeStore(Store store) {
    if(this.registeredStores.contains(store)){
      this.registeredStores.remove(this.registeredStores.indexOf(store));
    }
  }

}

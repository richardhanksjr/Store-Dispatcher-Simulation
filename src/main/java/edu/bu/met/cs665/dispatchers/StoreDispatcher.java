package edu.bu.met.cs665.dispatchers;

import edu.bu.met.cs665.customers.Customer;
import edu.bu.met.cs665.orders.BirthdayOrder;
import edu.bu.met.cs665.orders.Order;
import edu.bu.met.cs665.stores.Store;
import edu.bu.met.cs665.vehicles.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Creates a StoreDispatcher singleton for all classes to communicate with.
 * 
 * @author Richard Hanks
 *
 */


public class StoreDispatcher implements Dispatcher {
  List<Vehicle> registeredVehicles = new ArrayList<>();
  List<Store> registeredStores = new ArrayList<>();
  List<Order> ordersNotScheduled = new ArrayList<>();
  List<Order> ordersInTransit = new ArrayList<>();
  List<Order> ordersDeliveryComplete = new ArrayList<>();
  private boolean increasedTraffic = false;

  private static StoreDispatcher storeDispatcherInstance;

  static {
    storeDispatcherInstance = new StoreDispatcher();
  }


  // Restrict access to the constructor
  private StoreDispatcher() {

  }

  /**
   * Method for getting the singleton StoreDispatcher instance.
   * 
   * @return The singleton StoreDispatcher instance
   */
  public static StoreDispatcher getInstance() {
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
   * Method for registering a vehicle with the dispatcher. Upon successful registration with the
   * dispatcher, a map of is returned that assigns the vehicle to a distance from each store.
   */
  @Override
  public Map<Store, Integer> registerVehicle(Vehicle vehicle) {
    // Only register the vehicle if it isn't already registered
    if (!registeredVehicles.contains(vehicle)) {
      registeredVehicles.add(vehicle);
    }
    return makeRandomInitialDistanceAssignments();
  }

  /**
   * Method for dispatching vehicles to deliver all of the packages in the list of orders to be
   * delivered.
   * 
   * @postcondition The ordersNotScheduled List has been emptied and the orders have been
   *                "delivered" to their customer destinations
   */
  public void dispatchVehicles() {
    // While there are still orders that have not been scheduled for delivery
    Vehicle deliveryVehicle = null;
    int currentSmallestTotalDistance = 0;
    while (this.getOrdersNotScheduled().size() > 0) {
      // Remove any orders from the ordersNotScheduled list that are in the scheduled/in-transit
      // list. This is the way orders are removed from
      // the list of orders to be scheduled for transit and is removed at this point to avoid a
      // ConcurrentModificationException, as
      // detailed later in this method.
      this.ordersNotScheduled.removeAll(this.ordersInTransit);
      for (Order order : this.getOrdersNotScheduled()) {
        // Find the vehicle that is marked as available for deliveries and has the smallest total
        // distance
        for (Vehicle vehicle : this.registeredVehicles) {
          // If the order needs to be frozen and the total distance to travel > 2, check that the
          // current vehicle has a freezer
          // If vehicle is available for deliveries
          int totalVehicleDistance =
              StoreDispatcher.getTotalDistance(order.getStore(), order.getCustomer(), vehicle);
          // getStatus() tells us if the vehicle is available for getting deliveries
          if (vehicle.getStatus()) {
            // If this order is frozen and either the total distance is greater than two or if there
            // is a high traffic event
            if (order.getKeepFrozen() && (totalVehicleDistance > 2 || this.increasedTraffic)) {
              // Current vehicle is unable to transport this frozen product, so continue to the next
              // vehicle
              if (!vehicle.canTransportFrozen()) {
                continue;
              } else {
                System.out.println("***********************************");
                System.out.println("Frozen meal assigned to vehicle " + vehicle.getVIN());
                System.out.println("***********************************");

              }
            }
            // If this is the first time through the vehicle for loop, set deliveryVehicle to the
            // first instance
            if (deliveryVehicle == null) {
              deliveryVehicle = vehicle;
              // Current smallest total distance will be the first first vehicle we check by
              // default.
              currentSmallestTotalDistance = totalVehicleDistance;
            } else if (totalVehicleDistance < currentSmallestTotalDistance) {
              deliveryVehicle = vehicle;
              currentSmallestTotalDistance = totalVehicleDistance;
            }
          }
        }
        // If a delivery vehicle has been found, assign it to deliver this order
        if (deliveryVehicle != null) {
          // Assign order to vehicle
          deliveryVehicle.deliverOrder(order, currentSmallestTotalDistance);
          // Orders that are scheduled should be moved to the ordersInTransit list. After we are
          // done iterating through the orders,
          // we need to remove any orders from the ordersNotScheduled list that are now in transit.
          // Waiting to do this will help
          // avoid the ConcurrentModifacation Exemption that will happen if we try removing from the
          // ordersNotScheduled list while
          // iterating on it.
          this.ordersInTransit.add(order);
          // Notify the customer that their order is on the way.
          order.getCustomer().receiveMessage("Dear Customer, Order " + order.getOrderNumber() + " has been scheduled for delivery.  Thank you for doing business with us!");
          // Set deliveryVehicle back to null to begin the process check again
          deliveryVehicle = null;
          // Vehicle has been found, so break out of the vehicle for loop and back to the orders for
          // loop
          break;
        }
      }
    }
  }

  /**
   * After an order has been delivered, remove it from the list.
   * This is equivalent to saying the order process is complete.
   * @param order the order that has been delivered adn should be removed.
   */
  public void removeOrderFromInTransitOrders(Order order) {
    if (this.ordersInTransit.contains(order)) {
      this.ordersInTransit.remove(order);
    }
  }

  /**
   * When a vehicle has an updated status, it will send a message back to the dispatcher and this
   * message will be broadcast to std out.
   * 
   * @param message The message sent from a vehicle.
   */
  public void displayMessageFromVehicle(String message) {
    System.out.println(message);
    System.out.println(
        "----------------------------------------------------------------------------------------------\n");
  }

  /**
   * Given a store and customer instance, computes the total distance between the vehicle and the
   * store and the customer and the store. This is the rough approximation of how far the vehicle
   * will need to travel.
   * 
   * @param store The store from where the order will originate for delivery
   * @param customer The customer to whome the package will be delivered
   * @parm the vehicle which will deliver the order
   * @return the total distance the vehicle will travel to complete this delivery.
   */
  public static int getTotalDistance(Store store, Customer customer, Vehicle vehicle) {
    int customerToStoreDistance = customer.getDistanceFromEachStore().get(store);
    int vehicleToStoreDistance = vehicle.getDistancesFromEachStore().get(store);
    return customerToStoreDistance + vehicleToStoreDistance;
  }



  /**
   * Make a random distance assignment to all the stores.
   * @return A map where the keys are all of the registered stores and the values are randomly
   *         assigned Integer values with indicates the assigned distance in arbitrary units from
   *         the the given Store key.
   */
  public Map<Store, Integer> makeRandomInitialDistanceAssignments() {
    Map<Store, Integer> assignmentMapping = new HashMap<>();
    int maxDistanceFromStore = 20;
    int random;
    for (Store store : this.registeredStores) {
      random = new Random().nextInt(maxDistanceFromStore);
      assignmentMapping.put(store, random);
    }
    return assignmentMapping;
  }

  @Override
  public void removeVehicle(Vehicle vehicle) {
    // Only try removing a vehicle from the list if it is in the list
    if (this.registeredVehicles.contains(vehicle)) {
      this.registeredVehicles.remove(this.registeredVehicles.indexOf(vehicle));
    }
  }

  @Override
  public void dispatchVehicle(Vehicle vehicle, Order order) {

  }

  /**
   * Receives a new order from a store and adds it to the list of orders that have not been
   * scheduled.
   * 
   * @postcondition The given order has been added to the OrdersNotScheduled List
   * @param order the order to be added to the list of orders not scheduled
   */
  @Override
  public void receiveOrder(Order order) {
    if (!ordersNotScheduled.contains(order)) {
      ordersNotScheduled.add(order);
    }
  }

  @Override
  public void updateVehicleDistance(Vehicle vehicle, int distance) {

  }

  @Override
  public List<Vehicle> getRegisteredVehicles() {
    return this.registeredVehicles;
  }

  @Override
  public void registerStore(Store store) {
    if (!this.registeredStores.contains(store)) {
      this.registeredStores.add(store);
    }
  }

  @Override
  public void removeStore(Store store) {
    if (this.registeredStores.contains(store)) {
      this.registeredStores.remove(this.registeredStores.indexOf(store));
    }
  }

  /**
   * This is the method for toggling on and off traffic events into the system.
   */
  public void setIncreasedTraffic(boolean increased) {
    this.increasedTraffic = increased;
    System.out.println("************************************************************");
    if (this.increasedTraffic) {
      System.out.println("There is increased traffic at this time.");
    } else {
      System.out.println("Traffic levels are normal.");
    }
    System.out.println("************************************************************");
  }

  public void placeBirthdayOrder(BirthdayOrder order) {
    this.ordersNotScheduled.add(order);
  }

}

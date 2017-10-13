import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class LogAnalyzer
{
        //constants to be used when pulling data out of input
   private static final String START_TAG = "START";
   private static final int START_NUM_FIELDS = 3;
   private static final int START_SESSION_ID = 1;
   private static final int START_CUSTOMER_ID = 2;
   private static final String BUY_TAG = "BUY";
   private static final int BUY_NUM_FIELDS = 5;
   private static final int BUY_SESSION_ID = 1;
   private static final int BUY_PRODUCT_ID = 2;
   private static final int BUY_PRICE = 3;
   private static final int BUY_QUANTITY = 4;
   private static final String VIEW_TAG = "VIEW";
   private static final int VIEW_NUM_FIELDS = 4;
   private static final int VIEW_SESSION_ID = 1;
   private static final int VIEW_PRODUCT_ID = 2;
   private static final int VIEW_PRICE = 3;
   private static final String END_TAG = "END";
   private static final int END_NUM_FIELDS = 2;
   private static final int END_SESSION_ID = 1;

   //creates a map of sessions to customer ids
   private static void processStartEntry(
      final String[] words,
      final Map<String, List<String>> sessionsFromCustomer)
   {
      if (words.length != START_NUM_FIELDS)
      {
         return;
      }

      List<String> sessions = sessionsFromCustomer.get(words[START_CUSTOMER_ID]);
      if (sessions == null)
      {
         sessions = new LinkedList<>();
         sessionsFromCustomer.put(words[START_CUSTOMER_ID], sessions);
      }

      sessions.add(words[START_SESSION_ID]);
   }

    // creates a map of Views to session ids
   private static void processViewEntry(final String[] words, final Map<String, List<View>> viewsFromSession)
   {
       if (words.length != VIEW_NUM_FIELDS)
       {
           return;
       }
       View v = new View(words[VIEW_PRODUCT_ID], words[VIEW_PRICE]);
       List<View> sessions = viewsFromSession.get(words[VIEW_SESSION_ID]);
       if (sessions == null)
       {
           sessions = new LinkedList<>();
           viewsFromSession.put(words[VIEW_SESSION_ID], sessions);
       }

       sessions.add(v);
   }

   // creates a map of Buys to sessions ids
   private static void processBuyEntry(final String[] words, final Map<String, List<Buy>> buysFromSession)
   {
       if (words.length != BUY_NUM_FIELDS)
       {
           return;
       }

       Buy b = new Buy(words[BUY_PRODUCT_ID], words[BUY_PRICE], words[BUY_QUANTITY]);
       List<Buy> sessions = buysFromSession.get(words[BUY_SESSION_ID]);
       if (sessions == null)
       {
           sessions = new LinkedList<>();
           buysFromSession.put(words[BUY_SESSION_ID], sessions);
       }

       sessions.add(b);
   }

   private static void processEndEntry(final String[] words)
   {
      if (words.length != END_NUM_FIELDS)
      {
         return;
      }
   }

      //this is called by processFile below - its main purpose is
      //to process the data using the methods you write above
   private static void processLine(
      final String line,
      final Map<String, List<String>> sessionsFromCustomer,
      final Map<String, List<View>> viewsFromSession,
      final Map<String, List<Buy>> buysFromSession
      )
   {
      final String[] words = line.split("\\h");

      if (words.length == 0)
      {
         return;
      }

      switch (words[0])
      {
         case START_TAG:
            processStartEntry(words, sessionsFromCustomer);
            break;
         case VIEW_TAG:
            processViewEntry(words, viewsFromSession);
            break;
         case BUY_TAG:
            processBuyEntry(words, buysFromSession);
            break;
         case END_TAG:
            processEndEntry(words);
            break;
      }
   }

    //Prints the average number of items viewed by visitors that do not make a purchase
   private static void printAverageViewsWithoutPurchase(final Map<String, List<View>> viewsFromSession,
                                                        final Map<String, List<Buy>> buysFromSession)
   {

        double numViews = 0;
        double numSessions = 0;
        for(Map.Entry<String, List<View>> entry: viewsFromSession.entrySet())
        {
            boolean didBuy = false;
            for(Map.Entry<String, List<Buy>> buyEntry: buysFromSession.entrySet())
            {
                if(entry.getKey().equals(buyEntry.getKey()))
                {
                    didBuy = true;
                }
            }
            if(didBuy == false)
            {
                numViews += entry.getValue().size();
                numSessions += 1;
            }
        }
       double averageViews = numViews/numSessions;
       System.out.println("Average Views without Purchase: " + averageViews + "\n");

   }

    //prints the purchase price minus the average price of the items viewed during that session
   private static void printSessionPriceDifference(final Map<String, List<View>> viewsFromSession,
                                                   final Map<String, List<Buy>> buysFromSession)
   {
       System.out.println("Price Difference for Purchased Product by Session");
       for(Map.Entry<String, List<Buy>> entry: buysFromSession.entrySet())
       {
           double sum = 0;
           System.out.println(entry.getKey());
           List<View> sessions = viewsFromSession.get(entry.getKey());
           List<Buy> buySessions = entry.getValue();
           for(View sessionView: sessions)
           {
               sum += sessionView.getPrice();
           }
           double averageSum = sum / sessions.size();
           for(Buy sessionBuy: buySessions)
           {
               double priceDifference = sessionBuy.getPrice() - averageSum;
               System.out.println("\t" + sessionBuy.getProduct() + " " + priceDifference);
           }
       }

   }

    //prints the number of sessions in which that customer viewed that product if that product was purchased
   private static void printCustomerItemViewsForPurchase(final Map<String, List<String>> sessionsFromCustomer,
                                                         final Map<String, List<View>> viewsFromSession,
                                                         final Map<String, List<Buy>> buysFromSession)
   {
      System.out.println("\nNumber of Views for Purchased Product by Customer");
      for(Map.Entry<String, List<String>> entry: sessionsFromCustomer.entrySet())
      {
          String customer = "";
          LinkedList<Buy> bought = new LinkedList<Buy>();
          boolean didBuy = false;
          List<String> sessions = entry.getValue();
          for(Map.Entry<String, List<Buy>> buyEntry: buysFromSession.entrySet())
          {
              for(String s: sessions)
              {
                  if(s.equals(buyEntry.getKey())) {
                      didBuy = true;
                      customer = entry.getKey();
                      bought.addAll(buyEntry.getValue());
                  }
              }
          }
          if(didBuy)
          {
              System.out.println(customer);
              for (Buy b: bought)
              {
                  int counter = 0;
                  for(String s: sessions)
                  {
                      boolean viewedInSession = false;
                      List<View> views = viewsFromSession.get(s);
                      if(views != null) {
                          for (View v : views) {
                              if (v.getProduct().equals(b.getProduct())) {
                                  viewedInSession = true;
                              }
                          }
                          if (viewedInSession) {
                              counter += 1;
                          }
                      }
                  }
                  System.out.println("\t" + b.getProduct() + " " + counter);
              }
          }
      }

   }


   private static void printStatistics(final Map<String, List<String>> sessionsFromCustomer,
                                       final Map<String, List<View>> viewsFromSession,
                                       final Map<String, List<Buy>> buysFromSession)
   {
       printAverageViewsWithoutPurchase(viewsFromSession, buysFromSession);
       printSessionPriceDifference(viewsFromSession, buysFromSession);
       printCustomerItemViewsForPurchase(sessionsFromCustomer, viewsFromSession, buysFromSession);

		
   }


   private static void printOutExample(
      final Map<String, List<String>> sessionsFromCustomer,
      final Map<String, List<View>> viewsFromSession,
      final Map<String, List<Buy>> buysFromSession) 
   {
      //for each customer, get their sessions
      //for each session compute views
      for(Map.Entry<String, List<String>> entry: 
         sessionsFromCustomer.entrySet()) 
      {
         System.out.println(entry.getKey());
         List<String> sessions = entry.getValue();
         for(String sessionID : sessions)
         {
            System.out.println("\tin " + sessionID);
            List<View> theViews = viewsFromSession.get(sessionID);
            if(theViews != null) {
                for (View thisView : theViews) {
                    System.out.println("\t\tviewed " + thisView.getProduct());
                }
            }
         }
      }
   }


   //called in populateDataStructures
   private static void processFile(
      final Scanner input,
      final Map<String, List<String>> sessionsFromCustomer,
      final Map<String, List<View>> viewsFromSession,
      final Map<String, List<Buy>> buysFromSession
      )
   {
      while (input.hasNextLine())
      {
         processLine(input.nextLine(), sessionsFromCustomer, viewsFromSession, buysFromSession);
      }
   }

   //called from main - mostly just pass through important data structures
   private static void populateDataStructures(final String filename, final Map<String, List<String>> sessionsFromCustomer,
                                              final Map<String, List<View>> viewsFromSession,
                                              final Map<String, List<Buy>> buysFromSession)
    throws FileNotFoundException
   {
      try (Scanner input = new Scanner(new File(filename))) {
         processFile(input, sessionsFromCustomer, viewsFromSession, buysFromSession);
      }
   }


   private static String getFilename(String[] args)
   {
      if (args.length < 1)
      {
         System.err.println("Log file not specified.");
         System.exit(1);
      }

      return args[0];
   }

   public static void main(String[] args)
   {
      /* Map from a customer id to a list of session ids associated with
       * that customer.
       */
      final Map<String, List<String>> sessionsFromCustomer = new HashMap<>();
      final Map<String, List<View>> viewsFromSession = new HashMap<>();
      final Map<String, List<Buy>> buysFromSession = new HashMap<>();

      final String filename = getFilename(args);

      try
      {
         populateDataStructures(filename, sessionsFromCustomer, viewsFromSession, buysFromSession);
         printStatistics(sessionsFromCustomer, viewsFromSession, buysFromSession);
      }
      catch (FileNotFoundException e)
      {
         System.err.println(e.getMessage());
      }
   }
}

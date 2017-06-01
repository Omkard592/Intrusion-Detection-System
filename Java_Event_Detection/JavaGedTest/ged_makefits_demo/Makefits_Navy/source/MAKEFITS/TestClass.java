package MAKEFITS;

import java.awt.*;

public class TestClass
{

	public TestClass()
	{
	}



    public static void putToSleep(int millis)
    {

        try{
    		Thread tht = new Thread();
    		tht.sleep(millis);
        }
        catch(Exception e)
    		{};
    }

    public static void placeDots(Graph a)
    {

//1
    a.addNode("T7001", new Latitude(32, 30,"N"),new Longitude(120, 0,"W"), "CA" );
//2
    a.addNode("T7002", new Latitude(32, 30,"N"),new Longitude(120, 0,"W"), "CA" );
//3
    a.addNode("T7003", new Latitude(32, 0,"N"),new Longitude(119, 35,"W"), "CA" );
//4
    a.addNode("T7004", new Latitude(32, 45,"N"),new Longitude(120, 0,"W"), "CA" );
//5
    a.addNode("T7005", new Latitude(32, 45,"N"),new Longitude(120, 0,"W"), "CA" );
//6
   a.addNode("T7006", new Latitude(33, 30,"N"),new Longitude(120, 29,"W"), "CA" );
//7
    a.addNode("T7010", new Latitude(33, 30,"N"),new Longitude(123, 0,"W"), "CA" );
//8
    a.addNode("T1234", new Latitude(33, 10,"N"),new Longitude(123, 0,"W"), "CA" );
//9
    a.addNode("T12345", new Latitude(33, 10,"N"),new Longitude(123, 10,"W"), "CA" );
//10
    a.addNode("T70022",new Latitude(33, 10,"N"),new Longitude(123, 20,"W"), "CA" );

//11
    a.addNode("T7650", new Latitude(34, 10,"N"),new Longitude(123, 10,"W"), "CA" );

//12
    a.addNode("T7651", new Latitude(34, 20,"N"),new Longitude(123, 10,"W"), "CA" );
//13
    a.addNode("T7555", new Latitude(30, 0,"N"),new Longitude(121, 29,"W"), "CA" );
//14
    a.addNode("T7556", new Latitude(29, 58,"N"),new Longitude(121, 29,"W"), "CA" );
//15
    a.addNode("T7557", new Latitude(33, 38,"N"),new Longitude(121, 9,"W"), "CA" );
//16
    a.addNode("T7558", new Latitude(29, 48,"N"),new Longitude(122, 9,"W"), "CA" );
//17
    a.addNode("T7559", new Latitude(29, 57,"N"),new Longitude(122, 45,"W"), "CA" );
//18
    a.addNode("T7560", new Latitude(30, 57,"N"),new Longitude(123, 45,"W"), "CA" );
//19
    a.addNode("T7561", new Latitude(30, 57,"N"),new Longitude(123, 47,"W"), "CA" );

    }

    public static void sampleRun(Graph a)
    {

      // Test Longitude values

      a.addNode("Lt40Lg100", new Latitude(38, 0,"N"),new Longitude(100, 0,"W"), "CA" );
      a.addNode("Lt40Lg110", new Latitude(38, 0,"N"),new Longitude(110, 0,"W"), "CA" );
      a.addNode("Lt40Lg120", new Latitude(38, 0,"N"),new Longitude(120, 0,"W"), "CA" );
      a.addNode("Lt40Lg130", new Latitude(38, 0,"N"),new Longitude(130, 0,"W"), "CA" );
      a.addNode("L40Lg140", new Latitude(38, 0,"N"),new Longitude(140, 0,"W"), "CA" );



      a.addNode("Lt30Lg100", new Latitude(30, 0,"N"),new Longitude(100, 0,"W"), "CA" );
      a.addNode("Lt30Lg110", new Latitude(30, 0,"N"),new Longitude(110, 0,"W"), "CA" );
      a.addNode("Lt30Lg120", new Latitude(30, 0,"N"),new Longitude(120, 0,"W"), "CA" );
      a.addNode("Lt30Lg130", new Latitude(30, 0,"N"),new Longitude(130, 0,"W"), "CA" );
      a.addNode("L30Lg140", new Latitude(30, 0,"N"),new Longitude(140, 0,"W"), "CA" );

      a.addNode("Lt20Lg100", new Latitude(20, 0,"N"),new Longitude(100, 0,"W"), "CA" );
      a.addNode("Lt20Lg110", new Latitude(20, 0,"N"),new Longitude(110, 0,"W"), "CA" );
      a.addNode("Lt20Lg120", new Latitude(20, 0,"N"),new Longitude(120, 0,"W"), "JF" );
      a.addNode("Lt20Lg130", new Latitude(20, 0,"N"),new Longitude(130, 0,"W"), "HC" );
  //    a.addNode("Lt20Lg140", new Latitude(20, 0,"N"),new Longitude(140, 0,"W"), "CA" );

      a.addNode("Lt10Lg100", new Latitude(10, 0,"N"),new Longitude(100, 0,"W"), "CA" );
      a.setWarning("Lt10Lg100");
      a.addNode("Lt10Lg110", new Latitude(10, 0,"N"),new Longitude(110, 0,"W"), "CA" );
      a.setWarning("Lt10Lg110");
      a.addNode("Lt10Lg120", new Latitude(10, 0,"N"),new Longitude(120, 0,"W"), "CA" );
      a.setWarning("Lt10Lg120");
      a.addNode("Lt10Lg130", new Latitude(10, 0,"N"),new Longitude(130, 0,"W"), "CA" );
      a.setWarning("Lt10Lg130");
      a.addNode("Lt10Lg140", new Latitude(10, 0,"N"),new Longitude(140, 0,"W"), "CA" );
      a.setWarning("Lt10Lg140");

      a.addNode("Lt0Lg100", new Latitude(0, 0,"N"),new Longitude(100, 0,"W"), "CA" );
      a.addNode("Lt0Lg110", new Latitude(0, 0,"N"),new Longitude(110, 0,"W"), "CA" );
      a.addNode("Lt0Lg120", new Latitude(0, 0,"N"),new Longitude(120, 0,"W"), "CA" );
      a.addNode("Lt0Lg130", new Latitude(0, 0,"N"),new Longitude(130, 0,"W"), "CA" );
      a.addNode("Lt0Lg140", new Latitude(0, 0,"N"),new Longitude(140, 0,"W"), "CA" );


      a.addNode("Lt-10Lg100", new Latitude(-10, 0,"N"),new Longitude(100, 0,"W"), "CA" );
      a.addNode("Lt-10Lg110", new Latitude(-10, 0,"N"),new Longitude(110, 0,"W"), "CA" );
      a.addNode("Lt-10Lg120", new Latitude(-10, 0,"N"),new Longitude(120, 0,"W"), "CA" );
      a.addNode("Lt-10Lg130", new Latitude(-10, 0,"N"),new Longitude(130, 0,"W"), "CA" );
      a.addNode("Lt-10Lg140", new Latitude(-10, 0,"N"),new Longitude(140, 0,"W"), "CA" );

      a.changeLocation("Lt-10Lg140", new Latitude(-20, 0, "N"), new Longitude(130, 0, "W"));


      try
      {
        Thread t = new Thread();
        t.sleep(4000);
      }
      catch(Exception e)
      {
        System.out.println("This is a test");
      }

      a.unsetWarning("Lt10Lg140");

      try
      {
        Thread t = new Thread();
        t.sleep(4000);
      }
      catch(Exception e)
      {
        System.out.println("This is a test");
      }

         a.unsetWarning("Lt10Lg130");

    };



    public static void main(String args[])
    {


    Frame f = new Frame("LOCATION MAP");
    f.show();
    f.setSize(500,334);
//    TestClass tc = new TestClass();




    Graph a = Track.graph;
//    a.setSize(600,600);
    f.add("Center", a);
    f.setLocation(425,0);        // what does this do?
    a.init();
//    a.setDMZZone();
    a.validate();
    a.start();





    TestClass.sampleRun(a);
 //   TestClass.placeDots(a);
//    TestClass.placeDots(a);


    }

}

class Lat
{
	public String hemisphere;
    public float degrees;
}

package suraimu;

/**
 * MyClass - a class by (your name here)
 */
 class TankData
{
	public TankData(){
	}
	
	private String name ;
	private double velocity ;
	private double heading ;
	private double bearing ;
	private double distance ;
	private double energy ;
	
	private double prvVelocity ;
	private double prvHeading ;
	private double prvBearing ;
	private double prvDistance ;
	private double prvEnergy ;
	
	public boolean trackable = true ;
	
	public void setName(String s){
		
		name = s ;
	}

	public void setData(double v,double h,double b,double d,double e){
		
		velocity = v ;
		heading = h ;
		bearing = b ;
		distance = d ;
		energy = e ;
	}
	
	public void setPrvData(double v,double h,double b,double d,double e){
	
		prvVelocity = v;
		prvHeading = h;
		prvBearing = b ;
		prvDistance = d;
		prvEnergy = e ;
	}

	public String getName(){
		return name ;
	}
	
	public double getVelocity(){
		return velocity;
	}
	
	public double getHeading(){
		return heading;
	}
	
	public double getBearing(){
		return bearing ;
	}
	
	public double getDistance(){
		return distance;
	}
	
	public double getEnergy() {
		return energy;
	}
	
	public double getPrvVelocity(){
		return prvVelocity;
	}
	
	public double getPrvHeading(){
		return prvHeading;
	}
	
	public double getPrvBearing(){
		return prvBearing ;
	}
	
	public double getPrvDistance(){
		return prvDistance;
	}
	
	public double getPrvEnergy() {
		return prvEnergy;
	}
}

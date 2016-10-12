package suraimu;
import robocode.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.Color;

public class Suraimu extends AdvancedRobot
{
//	private LinkedList<TankData> list = new LinkedList<TankData>();
	private List<TankData> list = new ArrayList<TankData>();
	private TankData data ;
	
	private List<BulletInfo> bulletList = new ArrayList<BulletInfo>();
	private BulletInfo bulletInfo ;
	
	private String targetName = null ;
	private int targetIndex = 0 ;
	
	private String trackerName = null ;
	private int bulletCount = 0 ;
	
	private int trackfireCount = 0 ;
	private boolean removeComp = true;
	
	private int direction = 1 ;
	private int trunIndex = 1 ;

	public void run() {

		setAdjustGunForRobotTurn(true) ;
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		
		setColors(Color.cyan,Color.cyan,Color.red);
		setEventPriority("HitRobotEvent" , 80);
		setEventPriority("CustomEvent" , 99);		

		addCustomEvent(
			new Condition("removeBulletList") { 				

				public boolean test() {
					return (!bulletList.isEmpty() &&removeComp&& (getTime() >= (bulletList.get(0).getExpectTime() + 5)));
					
				};
			}
		);

		if(getX() <= (getBattleFieldWidth() / 2)){
		
			turnLeft(normalRelativeAngle(getHeading() + 90)) ;
			ahead(getX()-100);
		}
		else{
		
			turnLeft(normalRelativeAngle(getHeading() - 90)) ;
			ahead(getBattleFieldWidth() - getX() -100);
		}
			turnRight(90);
		while(true) {
				nearWall() ;
				setTurnRadarRight(360);
				waitFor(new RadarTurnCompleteCondition(this));
				setTurnRadarLeft(360);
				waitFor(new RadarTurnCompleteCondition(this));
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		
		boolean NameOverlap = false ;
		
		int listIndex = 0;

		if(list.isEmpty()){
			
			data = new TankData() ;
			
			data.setName(e.getName());
			data.setData(e.getVelocity(),e.getHeading(),e.getDistance(),e.getEnergy() );
			
			list.add(data);	
			
		}
		else{
			
			int c = 0 ;

			for(TankData d : list){
				
				if(d.getName().equals(e.getName())){
				
						NameOverlap = true;
						
						listIndex = c ;

						data.setPrvData(d.getVelocity(),d.getHeading(),d.getDistance(),d.getEnergy());
	
						data.setData(e.getVelocity(),e.getHeading(),e.getDistance(),e.getEnergy());
						
				}
				
				c ++ ;
			}
			
			if(!NameOverlap){
			
				data = new TankData() ;
				
				data.setName(e.getName());
				data.setData(e.getVelocity(),e.getHeading(),e.getDistance(),e.getEnergy());	
				
				list.add(data);	
				
			}
			
		}
	
		if(NameOverlap){
			
			data = list.get(listIndex);			
			
			//setTurnRight(normalRelativeAngle(e.getBearing() + 90)) ;
			//out.println(e.getBearing());
	/*		if((-15 <= e.getBearing() &&  e.getBearing() <= 15) || (165 <= e.getBearing() ||  e.getBearing() <= -165)){
							

				setTurnRight(90 * trunIndex);
				trunIndex *= -1 ;
				setAhead(70 * direction) ;
			
			}*/
						

			if((data.getPrvEnergy() != data.getEnergy()) && new MoveCompleteCondition(this).test()){		

				setAhead(70 * direction) ;
			/*	//nearWall() ;
				if(getX() >= (getBattleFieldWidth() / 2)  ){
				
					if(getY() <= 150)
						//setBack(getBattleFieldHeight() / 2);
						direction *= -1 ;
					//waitFor(new MoveCompleteCondition(this));
				}
				else if( getX() <= (getBattleFieldWidth() / 2)   ){
				
					if(getBattleFieldHeight() - 150 <= getY())
						direction *= -1 ;
					//	setBack(getBattleFieldHeight() / 2);
					//waitFor(new MoveCompleteCondition(this));
				}
				
				*/
			}
			//waitFor(new GunTurnCompleteCondition(this)) ;
			targetIndex = listIndex;
			trackFire(e , data);
			
		}
	
		//setColors(new Color(10*list.size(),10,10),new Color(10*list.size(),10,10),new Color(10*list.size(),10,10));
		
		if(getOthers() <= 1 ){
					
			setTurnRadarRight((normalRelativeAngle((getHeading() + e.getBearing()) - 
									getRadarHeading()  ) ));
			waitFor(new RadarTurnCompleteCondition(this));
			scan();
		}
	}
	
	public void trackFire(ScannedRobotEvent e , TankData d){
	
		double firePower ;
		double bulletVelocity ;

		if(targetName == null ){
			
			targetName = e.getName();
		}
			//out.println(targetName);
		if(targetName.equals(e.getName())){
		
			firePower = 400 / e.getDistance() ;
			bulletVelocity = 20 - 3 * firePower ;

			if(e.getHeading() == d.getPrvHeading() && e.getVelocity() == d.getPrvVelocity() && d.trackable){

				double enemyXdis 
							= Math.sin(Math.toRadians(normalRelativeAngle(getHeading() + e.getBearing())))* e.getDistance() ;
				double enemyYdis
							= Math.cos(Math.toRadians(normalRelativeAngle(getHeading() + e.getBearing())))* e.getDistance();			
				
				//out.println(enemyXdis +","+enemyYdis);
				
				double A = bulletVelocity * bulletVelocity - e.getVelocity() * e.getVelocity() ;

				double B = e.getVelocity()*((Math.sin(e.getHeadingRadians()) * enemyXdis) +
								(Math.cos(e.getHeadingRadians()) * enemyYdis));
									 
				double C = enemyXdis * enemyXdis + enemyYdis * enemyYdis ;
				
				
				
				double t1 = (B + Math.sqrt(B * B + A * C)) / A ;
				double t2 = (B - Math.sqrt(B * B + A * C)) / A ;
				double time = 0;
				
				if(t1 < 0){	
				
					if(t2 >= 0){
						
						time = t2;
					}
				}
				else{
				
					if(t2 < 0 || t1 < t2){
					
						time = t1 ;
					}
					else{
					
						time = t2 ;
					}
				}
	
				if(((time * e.getVelocity() * Math.sin(e.getHeadingRadians())) + getX() + enemyXdis) < 0 ){
				//out.println((getX() + enemyXdis));
					time = (getX() + enemyXdis) / e.getVelocity() ;
				}
			
				if(((time * e.getVelocity() * Math.sin(e.getHeadingRadians())) + getX() + enemyXdis) > getBattleFieldWidth() ){
				//out.println((getBattleFieldWidth() - getX() + enemyXdis));
					time = (getBattleFieldWidth() - (getX() + enemyXdis)) / e.getVelocity() ;
				}
				
				if(((time * e.getVelocity() * Math.cos(e.getHeadingRadians())) + getY() + enemyYdis) < 0 ){
				//out.println("-Y");
					time = (getY() + enemyYdis)/ e.getVelocity() ;
				}
			
				if(((time * e.getVelocity() * Math.cos(e.getHeadingRadians())) + getY() + enemyYdis) > getBattleFieldHeight() ){
				//out.println("Y");
					time = (getBattleFieldHeight() - (getY() + enemyYdis)) / e.getVelocity() ;
				}

			//	double predictionAngle = Math.toDegrees(Math.asin((enemyXdis + e.getVelocity() * time * Math.sin(e.getHeadingRadians())) / (bulletVelocity * time)));
		
				double ptan = Math.toDegrees(Math.atan((enemyXdis + e.getVelocity() * time * Math.sin(e.getHeadingRadians())) / (enemyYdis + e.getVelocity() * time * Math.cos(e.getHeadingRadians()))));

				if(!Double.isNaN(ptan)){				
	
					if(((e.getVelocity() * time * Math.cos(e.getHeadingRadians())) + enemyYdis + getY()) >= getY())	{
						turnGunRight(normalRelativeAngle(ptan - getGunHeading()));
						//out.println("uegawa");
					}
					else{
						turnGunRight(normalRelativeAngle(180 - getGunHeading() + ptan));
						//out.println("sitagawa");
					}
				}
				else	setTurnGunRight((normalRelativeAngle((getHeading() + e.getBearing()) - 
									getGunHeading()  ) ));
				//out.println("time:"+ time);
	
				if(getGunHeat() == 0) {
				
					Bullet bullet = fireBullet(firePower);
					trackBullet(time , bullet) ;
				/*	out.println("--------------------"+bulletList.size());
					int i = 0;
					while(i != bulletList.size()){
						
						out.println(bulletList.get(i).getExpectTime() +","+ i);
						i++;
					}*/
				}
			}
			
			else{
			
				setTurnGunRight((normalRelativeAngle((getHeading() + e.getBearing()) - 
									getGunHeading()  ) ));
				
				if(getGunHeat() == 0) fire(2);//fire(firePower);

			}
		}
	}

	public void trackBullet(double ht , Bullet b){
		
		bulletInfo = new BulletInfo();
		
		bulletInfo.setData(ht,getTime(),b);
		
		bulletList.add(bulletInfo);
		
		bulletList.sort(new BulletListComparator());

	}
	
	public void onCustomEvent(CustomEvent e){
			
		if (e.getCondition().getName().equals("removeBulletList")){
			
			removeComp = false;
			
			if(!bulletList.isEmpty()){

				BulletInfo bi = bulletList.get(0);
				bulletList.remove(0);
				if(bi.getBullet().isActive()){
					
					trackfireCount ++;
					out.println(trackfireCount);
				}
				else{
					
					trackfireCount = 0 ;
				}
				
				
				if(trackfireCount >= 5){
					
					list.get(targetIndex).trackable = false ;
				}
			}
			removeComp = true;
		}
	}
	
	public void onRobotDeath(RobotDeathEvent e){
	out.println("death");	
		if(e.getName().equals(targetName)) targetName = null ;
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		//clearAllEvents() ;
		//setTurnLeft(90) ;
		direction *= -1;
		
		if(0 <= e.getBearing() && e.getBearing() <= 180){
			
			turnLeft(90 - e.getBearing());
		}
		else{
			
			turnRight(90 + e.getBearing());			
		}
	/*	if(-90 <= e.getBearing() && e.getBearing() <= 90)
			setBack(300) ;
		else
			setAhead(300);*/
		//waitFor(new TurnCompleteCondition(this));
	}	
	
public void nearWall(){

		int wallDistance = 200 ;
		
		if(direction == 1){
			
			if(getX() < wallDistance && 180 < getHeading()){
				out.println("xl");
				if(getY() < getBattleFieldHeight() / 2){
					
					setTurnRight(360 - getHeading());
					setAhead(70 * direction);
				}
				else{
					
					setTurnRight(180 - getHeading());
					setAhead(70 * direction);
				}
			}
			if(getBattleFieldWidth() - wallDistance < getX() && getHeading() < 180){
				out.println("xr");
				if(getY() < getBattleFieldHeight() / 2){
					
					setTurnRight(-getHeading());
					setAhead(70 * direction);
				}
				else{
					
					setTurnRight(getHeading());
					setAhead(70 * direction);
				}
			}
			if(getY() < wallDistance && 90 < getHeading() && getHeading() < 270){
				out.println("ys");
				if(getX() < getBattleFieldWidth() / 2){
					
					setTurnRight(90 - getHeading());
					setAhead(70 * direction);
				}
				else{
					
					setTurnRight(270 - getHeading());
					setAhead(70 * direction);
				}
			}
			if(getBattleFieldHeight() - wallDistance < getY() &&( 90 > getHeading() || getHeading() > 270)){
				out.println("yu");
				if(getX() < getBattleFieldWidth() / 2){
					
					setTurnRight(normalAbsoluteAngle(450 - getHeading()));
					setAhead(70 * direction);
				}
				else{
					
					setTurnRight(normalRelativeAngle(-90 - getHeading()));
					setAhead(70 * direction);
				}
			}
				
		}
		
		else{
			
			if(getX() < wallDistance && 180 > getHeading()){
				out.println("xl");
				if(getY() < getBattleFieldHeight() / 2){
					
					setTurnRight(180 - getHeading());
					setAhead(70 * direction);
				}
				else{
					
					setTurnRight( -getHeading());
					setAhead(70 * direction);
				}
			}
			if(getBattleFieldWidth() - wallDistance < getX() && getHeading() > 180){
				out.println("xr");
				if(getY() < getBattleFieldHeight() / 2){
					
					setTurnRight(180 - getHeading());
					setAhead(70 * direction);
				}
				else{
					
					setTurnRight(360 - getHeading());
					setAhead(70 * direction);
				}
			}
			if(getY() < wallDistance && ( 90 > getHeading() || getHeading() > 270)){
				out.println("ys");
				if(getX() < getBattleFieldWidth() / 2){
					
					setTurnRight(normalRelativeAngle(-90 - getHeading()));
					setAhead(70 * direction);
				}
				else{
					
					setTurnRight(normalAbsoluteAngle(450 - getHeading()));
					setAhead(70 * direction);
				}
			}
			if(getBattleFieldHeight() - wallDistance < getY() && 90 < getHeading() && getHeading() < 270){
				out.println("yu");
				if(getX() < getBattleFieldWidth() / 2){
					
					setTurnRight(270 - getHeading());
					setAhead(70 * direction);
				}
				else{
					
					setTurnRight(90 - getHeading());
					setAhead(70 * direction);
				}
			}
				
		}
	/*	
		if(0 <= getX() && getX() <= getBattleFieldWidth()/2 && 0 <= getY() && getY() <= getBattleFieldWidth()/2){
			
			if(wallDistance > getX() ){
		
				if(getHeading() <= 90 || 270 <= getHeading()){
					
					setTurnRight(getHeading() - 180);
				}
				else{
					
					setTurnRight(180 - getHeading()) ;
				}
			}
			
			if(wallDistance > getY()){
				
				if(0 <= getHeading() && getHeading() <= 180){
					
					setTurnRight(90 - getHeading());
				}
				else{
					
					setTurnRight(270 - getHeading());
				}
			}
		}		
		
		if(0 <= getX() && getX() <= getBattleFieldWidth()/2 &&  getBattleFieldWidth()/2 <= getY() && getY() <= ge){
			 

		}*/
	}
	
	public void onHitByBullet(HitByBulletEvent e) {
		//clearAllEvents() ;
		//setAhead(70) ;
		//nearWall() ;
		//setTurnRight(normalRelativeAngle(e.getBearing() + 90)) ;

		if(trackerName == null || !(trackerName.equals(e.getName()))){
			
			trackerName = e.getName();
			bulletCount = 0 ;
		}		

		if(trackerName.equals(e.getName())){
			
			bulletCount ++ ;
		}
		if(bulletCount == 3){
			
			bulletCount = 0 ;
			targetName = trackerName;
		}
	}
	
	public void onHitRobot(HitRobotEvent e){
		
		targetName = e.getName();
		direction *= -1 ;
	} 

	public double normalRelativeAngle(double angle) {
		if (angle > -180 && angle <= 180)
			return angle;
		double fixedAngle = angle;
		while (fixedAngle <= -180)
			fixedAngle += 360;
		while (fixedAngle > 180)
			fixedAngle -= 360;
		return fixedAngle;
	}
	
	public double normalAbsoluteAngle(double angle) {
		if (angle >= 0 && angle < 360)
			return angle;
		double fixedAngle = angle;
		while (fixedAngle < 0)
			fixedAngle += 360;
		while (fixedAngle >= 360)
			fixedAngle -= 360;
		return fixedAngle;
	}
	
	public void onWin(WinEvent e) {
		clearAllEvents() ;
		setTurnGunRight(40000);
		for (int i = 0; i < 50; i++)
		{
			turnRight(40);
			turnLeft(40);
		}
	}
	
	
}

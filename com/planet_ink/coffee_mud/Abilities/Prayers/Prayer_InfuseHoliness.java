package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;


/* 
   Copyright 2000-2008 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Prayer_InfuseHoliness extends Prayer
{
	public String ID() { return "Prayer_InfuseHoliness"; }
	public String name(){return "Infuse Holiness";}
	public String displayText(){return "(Infused Holiness)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	public long flags(){return Ability.FLAG_HOLY;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOOD);
        if(CMath.bset(affectableStats.disposition(),EnvStats.IS_EVIL))
            affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_EVIL);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null))
			return;
		if(canBeUninvoked())
			if(affected instanceof MOB)
				((MOB)affected).tell("Your infused holiness fades.");

		super.unInvoke();

	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector<Object> commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORNREQ_ANY);
		if(target==null)
        {
            if((CMLib.law().doesOwnThisProperty(mob,mob.location()))
            &&(CMParms.combine(commands,0).equalsIgnoreCase("room")
                ||CMParms.combine(commands,0).equalsIgnoreCase("here")))
                target=mob.location();
            else
                return false;
        }

        Deity D=null;
		if(CMLib.law().getClericInfusion(target)!=null)
		{
            
            if(target instanceof Room) D=CMLib.law().getClericInfused((Room)target);
            if(D!=null)
    			mob.tell("There is already an infused aura of "+D.Name()+" around "+target.name()+".");
            else
                mob.tell("There is already an infused aura around "+target.name()+".");
			return false;
		}
        
        D=mob.getMyDeity();
        if(target instanceof Room)
        {
            if(D==null)
            {
                mob.tell("The faithless may not infuse holiness in a room.");
                return false;
            }
            Area A=mob.location().getArea();
            Room R=null;
            for(Enumeration<Room> e=A.getMetroMap();e.hasMoreElements();)
            {
                R=(Room)e.nextElement();
                if(CMLib.law().getClericInfused((Room)target)==D)
                {
                    mob.tell("There is already a holy place of "+D.Name()+" in this area at "+R.roomTitle()+".");
                    return false;
                }
            }
        }

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"A holy aura appears around <T-NAME>.":"^S<S-NAME> "+prayForWord(mob)+" to infuse a holy aura around <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
                if(D!=null) setMiscText(D.Name());
                if((target instanceof Room)
                &&(CMLib.law().doesOwnThisProperty(mob,((Room)target))))
                {
                    target.addNonUninvokableEffect((Ability)this.copyOf());
                    CMLib.database().DBUpdateRoom((Room)target);
                }
                else
    				beneficialAffect(mob,target,asLevel,0);
				target.recoverEnvStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to infuse a holy aura in <T-NAMESELF>, but fail(s).");

		return success;
	}
}

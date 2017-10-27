package com.terwaamo.futis;

public class TeamData {

	private class TData
	{
		String shortName;
		int id;
		String smallLogoFileName;
		
		public TData(String shn, int idNum, String file)
		{
			shortName = shn;
			id = idNum;
			smallLogoFileName = file;		
		}
				
	}
	
	final private TData[] dataArray = {
			new TData("HJK", 1, "hjk_144"),
			new TData("FC Honka", 27, "fc_honka_144"),
			new TData("VPS", 26, "vps_144"),
			new TData("IFK Mariehamn", 24, "ifk_144"),
			new TData("FC Lahti", 8, "fc_lahti_144"),
			new TData("MyPa", 10, "mypa_144"),
			new TData("KuPS", 25, "kups_144"),
			new TData("TPS", 2, "tps_144"),
			new TData("FC Inter", 11, "fc_inter_144"),
			new TData("FF Jaro", 17, "ff_jaro_144"),
			new TData("RoPS", 21, "rops_144"),
			new TData("SJK", 31, "sjk_144")			
			};
	
	public String getTeamShortDesc(int id)
	{
		for(int i = 0; i < dataArray.length; i++)
		{
			if(dataArray[i].id == id)
			{
				return dataArray[i].shortName;
			}
		}
		return null;
	}
	
	public String getTeamLogoFileName(int id)
	{
		for(int i = 0; i < dataArray.length; i++)
		{
			if(dataArray[i].id == id)
			{
				return dataArray[i].smallLogoFileName;
			}
		}
		return null;
	}
}

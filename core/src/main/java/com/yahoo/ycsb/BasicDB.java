/**                                                                                                                                                                                
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved.                                                                                                                             
 *                                                                                                                                                                                 
 * Licensed under the Apache License, Version 2.0 (the "License"); you                                                                                                             
 * may not use this file except in compliance with the License. You                                                                                                                
 * may obtain a copy of the License at                                                                                                                                             
 *                                                                                                                                                                                 
 * http://www.apache.org/licenses/LICENSE-2.0                                                                                                                                      
 *                                                                                                                                                                                 
 * Unless required by applicable law or agreed to in writing, software                                                                                                             
 * distributed under the License is distributed on an "AS IS" BASIS,                                                                                                               
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or                                                                                                                 
 * implied. See the License for the specific language governing                                                                                                                    
 * permissions and limitations under the License. See accompanying                                                                                                                 
 * LICENSE file.                                                                                                                                                                   
 */

package com.yahoo.ycsb;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.yahoo.ycsb.tsdb.DataPoint;
import com.yahoo.ycsb.tsdb.DataPointWithMetricID;


/**
 * Basic DB that just prints out the requested operations, instead of doing them against a database.
 */
public class BasicDB extends DB
{
	public static final String VERBOSE="basicdb.verbose";
	public static final String VERBOSE_DEFAULT="true";
	
    public static final String SIMULATE_DELAY="basicdb.simulatedelay";
    public static final String SIMULATE_DELAY_DEFAULT="0";
    
    public static final String RANDOMIZE_DELAY="basicdb.randomizedelay";
    public static final String RANDOMIZE_DELAY_DEFAULT="true";
    
	
    boolean verbose;
    boolean randomizedelay;
	int todelay;

	public BasicDB()
	{
		todelay=0;
	}

	
	void delay()
	{
		if (todelay>0)
		{
		    long delayNs;
		    if (randomizedelay) {
		        delayNs = TimeUnit.MILLISECONDS.toNanos(Utils.random().nextInt(todelay));
		        if (delayNs == 0) {
		            return;
		        }
		    }
		    else {
		        delayNs = TimeUnit.MILLISECONDS.toNanos(todelay);
		    }
		    
		    long now = System.nanoTime();
            final long deadline = now + delayNs;
            do {
                LockSupport.parkNanos(deadline - now);
            } while ((now = System.nanoTime()) < deadline && !Thread.interrupted());
		}
	}

	/**
	 * Initialize any state for this DB.
	 * Called once per DB instance; there is one DB instance per client thread.
	 */
	@SuppressWarnings("unchecked")
	public void init()
	{
		verbose=Boolean.parseBoolean(getProperties().getProperty(VERBOSE, VERBOSE_DEFAULT));
		todelay=Integer.parseInt(getProperties().getProperty(SIMULATE_DELAY, SIMULATE_DELAY_DEFAULT));
		randomizedelay=Boolean.parseBoolean(getProperties().getProperty(RANDOMIZE_DELAY, RANDOMIZE_DELAY_DEFAULT));
		if (verbose)
		{
			System.out.println("***************** properties *****************");
			Properties p=getProperties();
			if (p!=null)
			{
				for (Enumeration e=p.propertyNames(); e.hasMoreElements(); )
				{
					String k=(String)e.nextElement();
					System.out.println("\""+k+"\"=\""+p.getProperty(k)+"\"");
				}
			}
			System.out.println("**********************************************");
		}
	}

	/**
	 * Read a record from the database. Each field/value pair from the result will be stored in a HashMap.
	 *
	 * @param table The name of the table
	 * @param key The record key of the record to read.
	 * @param fields The list of fields to read, or null for all of them
	 * @param result A HashMap of field/value pairs for the result
	 * @return Zero on success, a non-zero error code on error
	 */
	public int read(String table, String key, Set<String> fields, HashMap<String,ByteIterator> result)
	{
		delay();

		if (verbose)
		{
			System.out.print("READ "+table+" "+key+" [ ");
			if (fields!=null)
			{
				for (String f : fields)
				{
					System.out.print(f+" ");
				}
			}
			else
			{
				System.out.print("<all fields>");
			}

			System.out.println("]");
		}

		return 0;
	}
	
	/**
	 * Perform a range scan for a set of records in the database. Each field/value pair from the result will be stored in a HashMap.
	 *
	 * @param table The name of the table
	 * @param startkey The record key of the first record to read.
	 * @param recordcount The number of records to read
	 * @param fields The list of fields to read, or null for all of them
	 * @param result A Vector of HashMaps, where each HashMap is a set field/value pairs for one record
	 * @return Zero on success, a non-zero error code on error
	 */
	public int scan(String table, String startkey, int recordcount, Set<String> fields, Vector<HashMap<String,ByteIterator>> result)
	{
		delay();

		if (verbose)
		{
			System.out.print("SCAN "+table+" "+startkey+" "+recordcount+" [ ");
			if (fields!=null)
			{
				for (String f : fields)
				{
					System.out.print(f+" ");
				}
			}
			else
			{
				System.out.print("<all fields>");
			}

			System.out.println("]");
		}

		return 0;
	}

	/**
	 * Update a record in the database. Any field/value pairs in the specified values HashMap will be written into the record with the specified
	 * record key, overwriting any existing values with the same field name.
	 *
	 * @param table The name of the table
	 * @param key The record key of the record to write.
	 * @param values A HashMap of field/value pairs to update in the record
	 * @return Zero on success, a non-zero error code on error
	 */
	public int update(String table, String key, HashMap<String,ByteIterator> values)
	{
		delay();

		if (verbose)
		{
			System.out.print("UPDATE "+table+" "+key+" [ ");
			if (values!=null)
			{
				for (String k : values.keySet())
				{
					System.out.print(k+"="+values.get(k)+" ");
				}
			}
			System.out.println("]");
		}

		return 0;
	}

	/**
	 * Insert a record in the database. Any field/value pairs in the specified values HashMap will be written into the record with the specified
	 * record key.
	 *
	 * @param table The name of the table
	 * @param key The record key of the record to insert.
	 * @param values A HashMap of field/value pairs to insert in the record
	 * @return Zero on success, a non-zero error code on error
	 */
	public int insert(String table, String key, HashMap<String,ByteIterator> values)
	{
		delay();

		if (verbose)
		{
			System.out.print("INSERT "+table+" "+key+" [ ");
			if (values!=null)
			{
				for (String k : values.keySet())
				{
					System.out.print(k+"="+values.get(k)+" ");
				}
			}

			System.out.println("]");
		}

		return 0;
	}


	/**
	 * Delete a record from the database. 
	 *
	 * @param table The name of the table
	 * @param key The record key of the record to delete.
	 * @return Zero on success, a non-zero error code on error
	 */
	public int delete(String table, String key)
	{
		delay();

		if (verbose)
		{
			System.out.println("DELETE "+table+" "+key);
		}

		return 0;
	}

    @Override
    public int insertDatapoints(final String table, final String measurement,
            TimeUnit timeUnit, final List<DataPointWithMetricID> datapoints) {
        delay();

        if (verbose)
        {
            System.out.println("INSERT datapoints "+table+" "+measurement+" "+datapoints);
        }

        return 0;
    }

    @Override
    public int scanDatapoints(final String table, final String key, final String field,
            final long startTime, final long endTime, final TimeUnit timeUnit,
            final Vector<DataPoint> result) {
        delay();

        if (verbose)
        {
            System.out.println("SCAN datapoints "+table+" "+key+" "+field+" from "+startTime+" to "+endTime);
        }
        return 0;
    }
	/**
	 * Short test of BasicDB
	 */
	/*
	public static void main(String[] args)
	{
		BasicDB bdb=new BasicDB();

		Properties p=new Properties();
		p.setProperty("Sky","Blue");
		p.setProperty("Ocean","Wet");

		bdb.setProperties(p);

		bdb.init();

		HashMap<String,String> fields=new HashMap<String,String>();
		fields.put("A","X");
		fields.put("B","Y");

		bdb.read("table","key",null,null);
		bdb.insert("table","key",fields);

		fields=new HashMap<String,String>();
		fields.put("C","Z");

		bdb.update("table","key",fields);

		bdb.delete("table","key");
	}*/
}

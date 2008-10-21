/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.logger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Vector;
import net.jot.testing.JOTTestable;

/**
 * Dumps an whole object hierarchy (fileds, values.. recursively)
 * Useful for debugging.
 *
 * @author thibautc
 */
public class JOTObjectDumper implements JOTTestable
{

    public static String dump(Object o)
    {
        StringBuffer buffer = new StringBuffer();
        dumpToSB(o, buffer, "");
        return buffer.toString();
    }

    public static void dumpToSB(Object o, StringBuffer buffer, String padding)
    {
        Class oClass = o.getClass();
        if (oClass.isArray())
        {
            buffer.append(padding).append("[").append(oClass.getName()).append("\n");
            for (int i = 0; i != Array.getLength(o); i++)
            {
                Object value = Array.get(o, i);
                dumpToSB(value, buffer, padding + "  ");
            }
            buffer.append(padding).append("]").append("\n");
        } else
        {
            buffer.append(padding).append("{").append(oClass.getName()).append("\n");
            while (oClass != null)
            {
                Field[] fields = oClass.getDeclaredFields();
                for (int i = 0; i != fields.length; i++)
                {
                    fields[i].setAccessible(true);
                    buffer.append(padding).append(fields[i].getName());
                    buffer.append(" = ");
                    try
                    {
                        Object value = fields[i].get(o);
                        if (value != null)
                        {
                            if (value.getClass().isArray())
                            {
                                dumpToSB(value, buffer, padding + "  ");
                            } else
                            {
                                buffer.append(value).append("\n");
                            }
                        }
                    } catch (IllegalAccessException e)
                    {
                    }
                }
                oClass = oClass.getSuperclass();
            }
            buffer.append(padding).append("}").append("\n");
        }
    }

    public void jotTest() throws Throwable
    {
        TestObject t=new TestObject();
        System.out.println(dump(t));
    }
    
    class TestObject
    {
        int field1=5;
        Integer field2=new Integer(2);
        String field3="field3";
        float field4=2.25f;
        byte[] b={1,2,3,4};
        Vector v=new Vector();
        
        Hashtable hash=new Hashtable();
        
        TestObject()
        {
            hash.put("blah",field3);
            hash.put("bloh",field2);
            hash.put("blut",b);
            
            v.add(field3);
            v.add(field2);
            v.add(b);
            v.add(hash);
        }
    }
    
    /*Serialization dump ??
     *
     * try
        {
            HeaderSalesDocument header = order.getHeader();
            PaymentBase payment = order.getPayment();
            PaymentBaseData pb = order.getPaymentData();
            //PaymentBaseData paymentBase=header.getPaymentData();
            //File f = new File("/tmp/b2cdump.txt");
            FileOutputStream f2 = new FileOutputStream("/tmp/b2cdump.xml");
            XMLEncoder encoder = new XMLEncoder(f2);
            encoder.writeObject(order);
            encoder.writeObject(order.getExtensionMap());
            encoder.writeObject(order.getHeaderBase());
            encoder.writeObject(order.getItems());
            encoder.writeObject(order.getLastEnteredCVVS());
            encoder.writeObject(order.getShipTos());

            encoder.writeObject(payment);
            encoder.writeObject(payment.getPaymentMethods());
            encoder.writeObject(payment.getPaymentTypes());
            encoder.writeObject(payment.getTechKey());
            encoder.writeObject(pb.getExtensionDataValues());
            encoder.writeObject(pb.getPaymentMethods());

            encoder.writeObject(header);
            encoder.writeObject(header.getAssignedCampaigns());
            encoder.writeObject(header.getAssignedCampaignsData());
            encoder.writeObject(header.getDeliveryPriority());
            encoder.writeObject(header.getIncoTerms1());
            encoder.writeObject(header.getDeliveryStatus());
            encoder.writeObject(header.getPartnerList());
            encoder.writeObject(header.getSalesOrg());
            encoder.writeObject(header.getPartnerList());
            encoder.writeObject(header.getReqDeliveryDate());
            encoder.writeObject(header.getShipTo());


            encoder.flush();
            f2.close();
       );
        } catch (Exception e)
        {
        }

     */
}

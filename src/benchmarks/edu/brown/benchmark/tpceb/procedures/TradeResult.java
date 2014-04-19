/***************************************************************************
 *  Copyright (C) 2009 by H-Store Project                                  *
 *  Brown University                                                       *
 *  Massachusetts Institute of Technology                                  *
 *  Yale University                                                        *
 *                                                                         *
 *  Original Version:                                                      *
 *  Zhe Zhang (zhe@cs.brown.edu)                                           *
 *  http://www.cs.brown.edu/~zhe/                                          *
 *                                                                         *
 *  Modifications by:                                                      *
 *  Andy Pavlo (pavlo@cs.brown.edu)                                        *
 *  http://www.cs.brown.edu/~pavlo/                                        *
 *                                                                         *
 *  Modifications by:                                                      *
 *  Alex Kalinin (akalinin@cs.brown.edu)                                   *
 *  http://www.cs.brown.edu/~akalinin/                                     *
 *                                                                         *
 *  Permission is hereby granted, free of charge, to any person obtaining  *
 *  a copy of this software and associated documentation files (the        *
 *  "Software"), to deal in the Software without restriction, including    *
 *  without limitation the rights to use, copy, modify, merge, publish,    *
 *  distribute, sublicense, and/or sell copies of the Software, and to     *
 *  permit persons to whom the Software is furnished to do so, subject to  *
 *  the following conditions:                                              *
 *                                                                         *
 *  The above copyright notice and this permission notice shall be         *
 *  included in all copies or substantial portions of the Software.        *
 *                                                                         *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,        *
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF     *
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. *
 *  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR      *
 *  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,  *
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR  *
 *  OTHER DEALINGS IN THE SOFTWARE.                                        *
 ***************************************************************************/
package edu.brown.benchmark.tpceb.procedures;

import java.util.Calendar;
import java.util.Date;

import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;
import org.voltdb.VoltTable;
import org.voltdb.VoltTableRow;
import org.voltdb.VoltType;
import org.voltdb.types.TimestampType;

/**
 * TradeResult Transaction <br/>
 * TPC-E Section 3.3.2b
 * 
 * H-Store exceptions:
 *   1) getTaxrate is modified from the specification to use a join instead of a "in" (sub-queries are not supported in H-Store)
 *   2) Some values retrieved from tables are not used, but required to be passed between frames. Those are "unused values" as in Java.
 */
public class TradeResult extends VoltProcedure {
    private final VoltTable trade_result_ret_template = new VoltTable(
            new VoltTable.ColumnInfo("acct_bal", VoltType.FLOAT)
    );

    public final SQLStmt getTrade = new SQLStmt("select T_CA_ID, T_TT_ID, T_S_SYMB, T_QTY, T_CHRG, T_LIFO, T_IS_CASH from TRADE where T_ID = ?");

    public final SQLStmt getTradeType = new SQLStmt("select TT_NAME, TT_IS_SELL, TT_IS_MRKT from TRADE_TYPE where TT_ID = ?");

    public final SQLStmt getHoldingSummary = new SQLStmt("select HS_QTY from HOLDING_SUMMARY where HS_CA_ID = ? and HS_S_SYMB = ?");

    //public final SQLStmt getCustomerAccount = new SQLStmt("select CA_B_ID, C_ID, CA_BAL, C_TIER from CUSTOMER_INFO where CA_ID = ?");
    public final SQLStmt getCustomerAccount = new SQLStmt("select CA_B_ID, C_ID, CA_BAL, C_TIER from CUSTOMER_INFO where CA_ID = ?");
    public final SQLStmt insertHoldingSummary = new SQLStmt("insert into HOLDING_SUMMARY(HS_CA_ID, HS_S_SYMB, HS_QTY) values (?, ?, ?)");

    public final SQLStmt updateHoldingSummary = new SQLStmt("update HOLDING_SUMMARY set HS_QTY = ? where HS_CA_ID = ? and HS_S_SYMB = ?");

    public final SQLStmt updateHolding = new SQLStmt("UPDATE holding SET h_qty = ? WHERE h_t_id = ?");

    public final SQLStmt getHoldingDesc = new SQLStmt("select H_T_ID, H_QTY, H_PRICE from HOLDING where H_CA_ID = ? and H_S_SYMB = ? order by H_DTS desc");

    public final SQLStmt getHoldingAsc = new SQLStmt("select H_T_ID, H_QTY, H_PRICE from HOLDING where H_CA_ID = ? and H_S_SYMB = ? order by H_DTS asc");

    public final SQLStmt insertHoldingHistory = new SQLStmt("insert into HOLDING_HISTORY (HH_H_T_ID, HH_T_ID, HH_BEFORE_QTY, HH_AFTER_QTY) " + "values (?, ?, ?, ?)");

    public final SQLStmt insertHolding = new SQLStmt("insert into HOLDING (H_T_ID, H_CA_ID, H_S_SYMB, H_DTS, H_PRICE, H_QTY) " + "values (?, ?, ?, ?, ?, ?)");

    public final SQLStmt deleteHoldingSummary = new SQLStmt("delete from HOLDING_SUMMARY where HS_CA_ID = ? and HS_S_SYMB = ?");

    public final SQLStmt deleteHolding = new SQLStmt("DELETE FROM holding WHERE h_t_id = ?");

    //public final SQLStmt getTaxrate = new SQLStmt("select sum(TX_RATE) from TAXRATE, CUSTOMER_TAXRATE where TX_ID = CX_TX_ID and CX_TX_ID = ?");

   // public final SQLStmt updateTrade1 = new SQLStmt("update TRADE set T_TAX = ? where T_ID = ?");

   public final SQLStmt getSecurity = new SQLStmt("select S_NAME from SECURITY where S_SYMB = ?");

   // public final SQLStmt getCustomer = new SQLStmt("select C_TIER from CUSTOMER where C_ID = ?");

   /* public final SQLStmt getCommissionRate = new SQLStmt("select CR_RATE from COMMISSION_RATE where CR_C_TIER = ? and CR_TT_ID = ? and CR_EX_ID = ? and CR_FROM_QTY <= ? and "
            + "CR_TO_QTY >= ? limit 1");*/

    public final SQLStmt updateTrade2 = new SQLStmt("update TRADE set T_COMM = ?, T_DTS = ?, T_ST_ID = ?, T_TRADE_PRICE = ? where T_ID = ?");

    public final SQLStmt insertTradeHistory = new SQLStmt("insert into TRADE_HISTORY (TH_T_ID, TH_DTS, TH_ST_ID) values (?, ?, ?)");

    public final SQLStmt updateBroker = new SQLStmt("update BROKER set B_COMM_TOTAL = B_COMM_TOTAL + ?, B_NUM_TRADES = B_NUM_TRADES + 1 where B_ID = ?");

    //public final SQLStmt insertSettlement = new SQLStmt("insert into SETTLEMENT (SE_T_ID, SE_CASH_TYPE, SE_CASH_DUE_DATE, SE_AMT) values (?, ?, ?, ?)");

    public final SQLStmt updateCustomerAccount = new SQLStmt("update CUSTOMER_INFO set CA_BAL=CA_BAL + ? where CA_ID = ?");

    public final SQLStmt insertCashTransaction = new SQLStmt("insert into CASH_TRANSACTION (CT_DTS, CT_T_ID, CT_AMT, CT_NAME) values (?, ?, ?, ?)");

    public VoltTable[] run(long trade_id, double trade_price, String st_completed_id) throws VoltAbortException {
        System.out.println("In trade result");
        // frame 1: collecting info
        // info about the trade
        System.out.println(trade_id);
        voltQueueSQL(getTrade, trade_id);
        VoltTable trade = voltExecuteSQL()[0];
        System.out.println("Successfully got trade row");
        
      //  assert trade.getRowCount() == 1;
        if(trade.getRowCount() == 0){
            System.out.println("Count was 0");
            
        }
        VoltTableRow trade_row = trade.fetchRow(0);
        System.out.println("got first row");
        long acct_id = trade_row.getLong("T_CA_ID");
        String type_id = trade_row.getString("T_TT_ID");
        String symbol = trade_row.getString("T_S_SYMB");
        int trade_qty = (int)trade_row.getLong("T_QTY");
        double charge = trade_row.getDouble("T_CHRG");
        int is_lifo = (int)(int)trade_row.getLong("T_LIFO");
        int trade_is_cash = (int)trade_row.getLong("T_IS_CASH");
        System.out.println("Successfully got all values from trade row");
        
        // info about a  type of the trade and customer's holdings for the symbol
        voltQueueSQL(getTradeType, type_id);
        voltQueueSQL(getHoldingSummary, acct_id, symbol);
        VoltTable[] trade_type_hold = voltExecuteSQL();
        System.out.println("Successfully got trade_type row and holding summary row");
        
        VoltTable trade_type = trade_type_hold[0];
        assert trade_type.getRowCount() == 1;
        VoltTableRow trade_type_row = trade_type.fetchRow(0);
        
        String type_name = trade_type_row.getString("TT_NAME");
        int type_is_sell = (int)(int)trade_type_row.getLong("TT_IS_SELL");
        int type_is_market = (int)trade_type_row.getLong("TT_IS_MRKT");
        System.out.println("Successfully got values from trade_type row ");
        
        VoltTable hold_sum = trade_type_hold[1];
        int hs_qty = 0;
        if (hold_sum.getRowCount() > 0) {
            assert hold_sum.getRowCount() == 1;
            hs_qty = (int)hold_sum.fetchRow(0).getLong("HS_QTY");
        }
        
        System.out.println("Successfully got values from holding summary row ");
        // frame 2: modifying the customer's holdings
        
        // first, some cusomer's account info
        voltQueueSQL(getCustomerAccount, acct_id);
        VoltTable account = voltExecuteSQL()[0];
        System.out.println("Successfully got cust acc info ");
        System.out.println(account.getRowCount());
        assert account.getRowCount() == 1;
        System.out.println("assertion passed");
        VoltTableRow account_row = account.fetchRow(0);
        System.out.println("going to get info");
        long broker_id = account_row.getLong("CA_B_ID");
        System.out.println("BID" +  broker_id);
      
       // long cust_id = account_row.getLong("CA_C_ID");
        //System.out.println("C ID" +  cust_id);
        //int c_tier = (int)account_row.getLong("C_TIER");
        //System.out.println("tier" +  c_tier);
        double acct_bal = account_row.getDouble("CA_BAL");
        System.out.println("acct_bal" +  acct_bal);
        System.out.println("Successfully got info from cust acc info ");
        
       // int tax_status = (int)account_row.getLong("CA_TAX_ST");
        
        int needed_qty = trade_qty;
        double buy_value = 0;
        double sell_value = 0;
       // Date trade_dts = Calendar.getInstance().getTime();
        long trade_dts =  Calendar.getInstance().getTimeInMillis();
       // TimestampType timestamp = new TimestampType(trade_dts);
        if (type_is_sell == 1) {
            System.out.println("type was sell");
            if (hs_qty == 0) {
                System.out.println("hs qty was 0");
                System.out.println("Acct ID" + acct_id);
                System.out.println("symbol" + symbol);
                System.out.println("trade qty" + trade_qty);
                //new SQLStmt("insert into HOLDING_SUMMARY(HS_CA_ID, HS_S_SYMB, HS_QTY) values (?, ?, ?)");
                voltQueueSQL(insertHoldingSummary, acct_id, symbol, -trade_qty);//was -ve
                voltExecuteSQL();
                System.out.println("insert holding summary");
            }
            else if (hs_qty != trade_qty) {
                System.out.println("qtys not equal");
                voltQueueSQL(updateHoldingSummary, hs_qty - trade_qty, acct_id, symbol);
                
                voltExecuteSQL();
                
                System.out.println("update holding summary");
            }
            System.out.println("done with the first if");
            if (hs_qty > 0) {
                System.out.println("qty greater than 0");
                if (is_lifo == 1) {
                    System.out.println("lifo is 1");
                    voltQueueSQL(getHoldingDesc, acct_id, symbol);
                }
                else {
                    System.out.println("lifo not 1");
                    voltQueueSQL(getHoldingAsc, acct_id, symbol);
                }
                
                // modify existing holdings
                VoltTable hold_list = voltExecuteSQL()[0];
                System.out.println("execute hold list");
                for (int i = 0; i < hold_list.getRowCount() && needed_qty != 0; i++) {
                    VoltTableRow hold = hold_list.fetchRow(i);
                    
                    long hold_id = hold.getLong("H_T_ID");
                    int hold_qty = (int)hold.getLong("H_QTY");
                    double hold_price = hold.getDouble("H_PRICE");
                    
                    if (hold_qty > needed_qty) {
                        voltQueueSQL(insertHoldingHistory, hold_id, trade_id, hold_qty, hold_qty - needed_qty);
                        voltQueueSQL(updateHolding, hold_qty - needed_qty, hold_id);
                        
                        buy_value += needed_qty * hold_price;
                        sell_value += needed_qty * trade_price;
                        needed_qty = 0;
                    }
                    else {
                        voltQueueSQL(insertHoldingHistory, hold_id, trade_id, hold_qty, 0);
                        voltQueueSQL(deleteHolding, hold_id);
                        
                        buy_value += hold_qty * hold_price;
                        sell_value += hold_qty * trade_price;
                        needed_qty = needed_qty - hold_qty;
                    }
                }
                // execute all updates from the above loop
                voltExecuteSQL();
                System.out.println("executed all updates");
            }
           
                
            // need to sell more? go short
            if (needed_qty > 0) {
                System.out.println("needed qyu g than 0");
                System.out.println("trade id"+ trade_id);
                System.out.println("acct_id"+ acct_id);
                System.out.println("symbol"+symbol);
                System.out.println("trade_dts"+ trade_dts);
                System.out.println("trade_price"+ trade_price);
                System.out.println("needed qty"+ needed_qty);
                //SQLStmt("insert into HOLDING_HISTORY (HH_H_T_ID, HH_T_ID, HH_BEFORE_QTY, HH_AFTER_QTY) " + "values (?, ?, ?, ?)");
                //QLStmt("insert into HOLDING (H_T_ID, H_CA_ID, H_S_SYMB, H_DTS, H_PRICE, H_QTY) " + "values (?, ?, ?, ?, ?, ?)");
                voltQueueSQL(insertHoldingHistory, trade_id, trade_id, 0, -needed_qty);
                System.out.println("inserted HH");
                voltQueueSQL(insertHolding, trade_id, acct_id, symbol, trade_dts, trade_price, -needed_qty); //trade_dts
                System.out.println("inserted HH");
             //   try{
                    voltExecuteSQL();
               //     }
               ///     catch(Exception ex){
               ///         ex.getMessage();
               //         ex.getCause();
               //         ex.printStackTrace();
               //     }
                System.out.println("short sell done");
                
            }
            else if (hs_qty == trade_qty) {
                System.out.println("h qty = t qty");
                voltQueueSQL(deleteHoldingSummary, acct_id, symbol);
                voltExecuteSQL();
                System.out.println("delete holding summary");
            }
        }
        else { // buy trade
            System.out.println("buying trade");
            if (hs_qty == 0) {
                System.out.println("hs is 0");
                voltQueueSQL(insertHoldingSummary, acct_id, symbol, trade_qty);
                voltExecuteSQL();
                System.out.println("insert buy trade holding summary");
            }
            else if (-hs_qty != trade_qty) {
                System.out.println("-ve hs qty equal to trade qty");
                System.out.println("Symbol:"+ symbol);
                System.out.println("HSQty"+ hs_qty);
                System.out.println("TQty"+ trade_qty);
                System.out.println("acctid"+ acct_id);
                voltQueueSQL(updateHoldingSummary, hs_qty + trade_qty, acct_id, symbol);
                voltExecuteSQL();
                System.out.println("update holding summary");
            }
            
            if (hs_qty < 0) {
                System.out.println("qty less than 0");
                if (is_lifo == 1) {
                    System.out.println("lifo is 1");
                    voltQueueSQL(getHoldingDesc, acct_id, symbol);
                }
                else {
                    System.out.println("doing holding asc");
                    voltQueueSQL(getHoldingAsc, acct_id, symbol);
                }
                
                // modify existing holdings
                VoltTable hold_list = voltExecuteSQL()[0];
                System.out.println("executing sql");
                for (int i = 0; i < hold_list.getRowCount() && needed_qty != 0; i++) {
                    VoltTableRow hold = hold_list.fetchRow(i);
                    
                    long hold_id = hold.getLong("H_T_ID");
                    int hold_qty = (int)hold.getLong("H_QTY");
                    double hold_price = hold.getDouble("H_PRICE");
                    
                    if (hold_qty + needed_qty < 0) {
                        voltQueueSQL(insertHoldingHistory, hold_id, trade_id, hold_qty, hold_qty + needed_qty);
                        voltQueueSQL(updateHolding, hold_qty + needed_qty, hold_id);
                        
                        sell_value += needed_qty * hold_price;
                        buy_value += needed_qty * trade_price;
                        needed_qty = 0;
                    }
                    else {
                        voltQueueSQL(insertHoldingHistory, hold_id, trade_id, hold_qty, 0);
                        voltQueueSQL(deleteHolding, hold_id);
                        
                        hold_qty = -hold_qty;
                        sell_value += hold_qty * hold_price;
                        buy_value += hold_qty * trade_price;
                        needed_qty = needed_qty - hold_qty;
                    }
                }
                // execute all updates from the above loop
                voltExecuteSQL();
                System.out.println("did next set of updates");
            }
                
            // all shorts are covered? a new long is created
            if (needed_qty > 0) {
                System.out.println("needed qty > 0");
                System.out.println("Trade id:" + trade_id);
                System.out.println("acct_id"+ acct_id);
                voltQueueSQL(insertHoldingHistory, trade_id, trade_id, 0, needed_qty);
                System.out.println("this worked");
                voltQueueSQL(insertHolding, trade_id, acct_id, symbol, trade_dts, (float)trade_price, needed_qty);
                voltExecuteSQL();
            }
            else if (-hs_qty == trade_qty) {
                System.out.println("deleting summ");
                voltQueueSQL(deleteHoldingSummary, acct_id, symbol);
                voltExecuteSQL();
            }
        }
        System.out.println("Successfully did insertions/deletions ");
        // frame 3: taxes
        double tax_amount = 10;
       /* if ((tax_status == 1 || tax_status == 2) && sell_value > buy_value) {
            voltQueueSQL(getTaxrate, cust_id);
            VoltTable tax_rate = voltExecuteSQL()[0];
            
            assert tax_rate.getRowCount() == 1;
            double tax_rates = tax_rate.fetchRow(0).getDouble(0);
            tax_amount = (sell_value - buy_value) * tax_rates;
            
            voltQueueSQL(updateTrade1, tax_amount, trade_id);
            voltExecuteSQL();
        }*/
        
        // frame 4: calculate the broker's commission
        voltQueueSQL(getSecurity, symbol);
      //  voltQueueSQL(getCustomer, cust_id);
        VoltTable[] sec_cust = voltExecuteSQL();
        System.out.println("Successfully got security info");
       // System.out.println(sec_cust[0]);
        VoltTable sec = sec_cust[0];
        System.out.println("got rows1");
       // VoltTable cust = sec_cust[1];
       // System.out.println("got rows");
        assert sec.getRowCount() == 1;
        System.out.println("security assertion ok");
       // assert cust.getRowCount() == 1;
        System.out.println("cust assertion ok");
        VoltTableRow sec_row = sec.fetchRow(0);
      //  String s_ex_id = sec_row.getString("S_EX_ID");
        String s_name = sec_row.getString("S_NAME");
        System.out.println("Successfully got sec info from row");
      //  int c_tier = (int)cust.fetchRow(0).getLong("C_TIER");
        
       // voltQueueSQL(getCommissionRate, c_tier, type_id, s_ex_id, trade_qty, trade_qty); // limit to 1 row
       // VoltTable comm = voltExecuteSQL()[0];
        
       // assert comm.getRowCount() == 1;
       // double comm_rate = comm.fetchRow(0).getDouble("CR_RATE");
        
        // frame 5: recording the results
        double comm_amount = 10;//(comm_rate / 100) * (trade_qty * trade_price);
        
        //SQLStmt("update TRADE set T_COMM = ?, T_DTS = ?, T_ST_ID = ?, T_TRADE_PRICE = ? where T_ID = ?");
        voltQueueSQL(updateTrade2, comm_amount, trade_dts, st_completed_id, trade_price, trade_id);
        System.out.println("queued update trade info");
        //new SQLStmt("insert into TRADE_HISTORY (TH_T_ID, TH_DTS, TH_ST_ID) values (?, ?, ?)");
        System.out.println("ST ID" + st_completed_id);
        voltQueueSQL(insertTradeHistory, trade_id, trade_dts, st_completed_id);
        System.out.println("inserted trade history sql");
        //new SQLStmt("update BROKER set B_COMM_TOTAL = B_COMM_TOTAL + ?, B_NUM_TRADES = B_NUM_TRADES + 1 where B_ID = ?");
        voltQueueSQL(updateBroker, comm_amount, broker_id);
        System.out.println("update broker info sql");
        voltExecuteSQL();
        System.out.println("Successfully did updates/insertions/deletions ");
        // frame 6: settling the trade
        Calendar cal = Calendar.getInstance();
        //cal.setTime(trade_dts);
        cal.setTimeInMillis(trade_dts);
        cal.add(Calendar.DATE, 2); // the settlement is due in two days
        
        TimestampType due_date = new TimestampType(cal.getTime());
        double se_amount;
        
        if (type_is_sell == 1) {
            se_amount = (trade_qty * trade_price) - charge - comm_amount;
        }
        else {
            se_amount = -((trade_qty * trade_price) + charge + comm_amount);
        }
        
      /*  if (tax_status == 1) {
            se_amount = se_amount - tax_amount;
        }*/
        
        String cash_type;
        if (trade_is_cash == 1) {
            cash_type = "Cash Account";
        }
        else {
            cash_type = "Margin";
        }
        
     //   voltQueueSQL(insertSettlement, trade_id, cash_type, due_date, se_amount);
        
        if (trade_is_cash == 1) {
            voltQueueSQL(updateCustomerAccount, se_amount, acct_id);
            voltQueueSQL(insertCashTransaction, trade_dts, trade_id, se_amount, type_name +  " " + trade_qty + " shares of " + s_name);
        }
        System.out.println("Successfully did updates/insertions/deletions 2 ");
       // voltQueueSQL(getCustomerAccountBalance, acct_id);
       // VoltTable bal = voltExecuteSQL()[0];
        
       // assert bal.getRowCount() == 1;
       // double acct_bal = bal.fetchRow(0).getDouble("CA_BAL");
        
        VoltTable ret_values = trade_result_ret_template.clone(64);
        ret_values.addRow((float)acct_bal);
        System.out.println("Successfully did update volt table ");
        return new VoltTable[] {ret_values};
    }
}

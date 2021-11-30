//package clientTest.xmallTest;
//
//import java.util.logging.Logger;
//
//import com.baidu.xuper.api.Account;
//
//import org.junit.Test;
//
//import auth.account;
//import client.base.base_def.*;
//import client.xmall.mall;
//import clientTest.xassetTest.config;
//
//public class MallTest {
//    @Test
//    public void TestSellItem() throws Exception {
//        Account acc = Account.create("private_key path");
//        SaleItemInfo info = new SaleItemInfo(0, 0, 0, 0, null);
//        SellItemParam param = new SellItemParam(acc, 124700183551579204L, info);
//
//        mall handle = new mall(config.getXassetConfig(), Logger.getGlobal());
//        SellItem result = handle.SellItem(param);
//        System.out.println(result.resp.saleId);
//    }
//
//    @Test
//    public void TestWithdrawItem() throws Exception {
//        Account acc = Account.create("private_key path");
//        WithdrawItemParam param = new WithdrawItemParam(124700183551579204L, 36577908588057668L, acc);
//
//        mall handle = new mall(config.getXassetConfig(), Logger.getGlobal());
//        Base result = handle.WithdrawItem(param);
//        System.out.println(result);
//    }
//
//    @Test
//    public void TestListByFilterItem() throws Exception {
//        ListByFilterItemParam param = new ListByFilterItemParam(100200, null, null, 0);
//
//        mall handle = new mall(config.getXassetConfig(), Logger.getGlobal());
//        ListCursor result = handle.ListByFilterItem(param);
//        System.out.println(result);
//    }
//
//    @Test
//    public void TestQueryItem() throws Exception {
//        QueryItemParam param = new QueryItemParam(124700183551579204L);
//
//        mall handle = new mall(config.getXassetConfig(), Logger.getGlobal());
//        QueryItem result = handle.QueryItem(param);
//        System.out.println(result);
//    }
//
//    @Test
//    public void TestListItems() throws Exception {
//        Account acc = Account.create("private_key path");
//        ListItemsParam param = new ListItemsParam(acc, 0, 1, 0);
//
//        mall handle = new mall(config.getXassetConfig(), Logger.getGlobal());
//        ListPage result = handle.ListItems(param);
//        System.out.println(result);
//    }
//
//    @Test
//    public void TestCreateOrder() throws Exception {
//        Account acc1 = account.NewXchainEcdsaAccount(2, 1);
//        Account acc2 = Account.create("private_key path");
//        CreateOrderParam param = new CreateOrderParam(116952242938221636L, 124700183551579204L, 24747678869263428L, 0,
//                acc1.getAKAddress(), mall_def.PayBySpecial, 1, 0, 0, 0, null, null, acc2);
//
//        mall handle = new mall(config.getXassetConfig(), Logger.getGlobal());
//        Base result = handle.CreateOrder(param);
//        System.out.println(acc1.getKeyPair().getJSONPrivateKey());
//        System.out.println(result);
//    }
//
//    @Test
//    public void TestCancleOrder() throws Exception {
//        Account acc = Account.create("private_key path");
//        CancelOrderParam param = new CancelOrderParam(0, 37297478818895016L, acc);
//
//        mall handle = new mall(config.getXassetConfig(), Logger.getGlobal());
//        Base result = handle.CancelOrder(param);
//        System.out.println(result);
//    }
//
//    @Test
//    public void TestDeleteOrder() throws Exception {
//        Account acc = Account.create("private_key path");
//        DeleteOrderParam param = new DeleteOrderParam(37297478818895016L, acc);
//
//        mall handle = new mall(config.getXassetConfig(), Logger.getGlobal());
//        Base result = handle.DeleteOrder(param);
//        System.out.println(result);
//    }
//
//    @Test
//    public void TestPayInfo() throws Exception {
//        Account acc = Account.create("private_key path");
//        PayInfoParam param = new PayInfoParam(116952242938221636L, mall_def.PayBySpecial, acc);
//
//        mall handle = new mall(config.getXassetConfig(), Logger.getGlobal());
//        PayInfo result = handle.PayInfo(param);
//        System.out.println(result);
//    }
//
//    @Test
//    public void TestQueryOrder() throws Exception {
//        Account acc = Account.create("private_key path");
//        QueryOrderParam param = new QueryOrderParam(116952242938221636L, 0, acc);
//
//        mall handle = new mall(config.getXassetConfig(), Logger.getGlobal());
//        QueryOrder result = handle.QueryOrder(param);
//        System.out.println(result);
//    }
//
//    @Test
//    public void TestQueryOrderList() throws Exception {
//        Account acc = Account.create("private_key path");
//        QueryOrderListParam param = new QueryOrderListParam(0, 0, null, 0, acc);
//
//        mall handle = new mall(config.getXassetConfig(), Logger.getGlobal());
//        ListCursor result = handle.QueryOrderList(param);
//        System.out.println(result);
//    }
//
//    @Test
//    public void TestListByStatus() throws Exception {
//        ListBystatusParam param = new ListBystatusParam(0, 0, 1000000000000L, null, 20);
//
//        mall handle = new mall(config.getXassetConfig(), Logger.getGlobal());
//        ListCursor result = handle.ListByStatus(param);
//        System.out.println(result);
//    }
//
//    @Test
//    public void TestPayNotify() throws Exception {
//        Account acc = Account.create("private_key path");
//        PayNotifyParam param = new PayNotifyParam(116952242938221636L, null, null, acc);
//
//        mall handle = new mall(config.getXassetConfig(), Logger.getGlobal());
//        Base result = handle.PayNotify(param);
//        System.out.println(result);
//    }
//}

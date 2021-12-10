package com.baidu.xasset.utils;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.Assert.assertNotEquals;

public class UtilsTest {
    @Test
    public void TestGenAssetId() throws IOException {
        for (int j = 0; j < 10; j++) {
            long assetId = Utils.genAssetId(300100);
            System.out.println(assetId);
        }
    }

    @Test
    public void TestGenRandId() {
        long randId = Utils.genRandId();
        assertNotEquals(randId, 0);
    }

    @Test
    public void TestGenNonce() {
        long nonce = Utils.genNonce();
        assertNotEquals(nonce, 0);
    }
}

package org.n3r.diamond.sdk.utils;


import com.google.common.collect.Lists;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.diamond.sdk.DiamondSDK;
import org.n3r.diamond.sdk.domain.ContextResult;
import org.n3r.diamond.sdk.domain.DiamondConf;
import org.n3r.diamond.sdk.domain.DiamondSDKConf;

public class DiamondSDKTest {
    private static DiamondSDK diamondSDK;

    @BeforeClass
    public static void beforeClass() {
        DiamondConf diamondConf = new DiamondConf("localhost", 8080, "admin", "libai123");
        DiamondSDKConf diamondSDKConf = new DiamondSDKConf(Lists.newArrayList(diamondConf));

        diamondSDK = new DiamondSDK(diamondSDKConf);
    }

    @AfterClass
    public static void afterClass() {
        diamondSDK.close();
    }

    @Test
    public void testPost() {
        ContextResult contextResult = diamondSDK.get("GROUP.TESTxxx", "DATAID.TEST");
        System.out.println(contextResult);
        if (contextResult.isSuccess() && contextResult.getStone() != null)
            diamondSDK.delete(contextResult.getStone().getId());

        contextResult = diamondSDK.post("GROUP.TESTxxx", "DATAID.TEST", "CONTEXT.TEST.XXXX");
        System.out.println(contextResult);
    }

    @Test
    public void testGet() {
        ContextResult contextResult = diamondSDK.get("EQL.CACHE", "com.raiyee.poet.security.service.MenuService.eql");
        System.out.println(contextResult);
    }

    @Test
    public void testUpdate() {
        ContextResult update = diamondSDK.update("GROUP.TESTxxx", "DATAID.TEST", "CONTEXT.TEST.YYYY");
        System.out.println(update);
    }
}

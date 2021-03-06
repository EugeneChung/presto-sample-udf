package io.github.yuokada.presto.udf.scalar;

import com.google.common.collect.ImmutableList;
import io.prestosql.operator.scalar.AbstractTestFunctions;
import io.prestosql.spi.type.ArrayType;
import io.prestosql.spi.type.VarcharType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class KuromojiUDFTest
        extends AbstractTestFunctions
{
    @DataProvider(name = "query-provider")
    public Object[][] dataProvider()
    {
        return new Object[][] {
                {"kuromoji_tokenize('5000兆円欲しい。')", 10,
                 ImmutableList.of("5000", "兆", "円", "欲しい")},
                {"kuromoji_tokenize('一風堂のラーメンが食べたい。')", 14,
                 ImmutableList.of("一風", "堂", "ラーメン", "食べる")},
                };
    }

    @DataProvider(name = "query-provider2")
    public Object[][] dataProvider2()
    {
        return new Object[][] {
                {"kuromoji_tokenize('5000兆円欲しい。', 'normal')", 10,
                 ImmutableList.of("5000", "兆", "円", "欲しい")},
                {"kuromoji_tokenize('5000兆円欲しい。', 'search')", 10,
                 ImmutableList.of("5000", "兆", "円", "欲しい")},
                {"kuromoji_tokenize('5000兆円欲しい。', 'extended')", 10,
                 ImmutableList.of("兆", "円", "欲しい")},
                };
    }

    @BeforeClass
    public void setUp()
    {
        registerScalar(KuromojiUDF.class);
    }

    @Test(threadPoolSize = 5, invocationCount = 10)
    public void testKuromojiNormal()
            throws Exception
    {
        assertFunction("kuromoji_tokenize('5000兆円欲しい。')",
                new ArrayType(VarcharType.createVarcharType(10)),
                ImmutableList.of("5000", "兆", "円", "欲しい"));
    }

    @Test(dataProvider = "query-provider")
    public void testKuromojiNormalDP(String query, Integer size, List<String> expect)
            throws Exception
    {
        assertFunction(query, new ArrayType(VarcharType.createVarcharType(size)), expect);
    }

    @Test(dataProvider = "query-provider2")
    public void testKuromojiNormalWithMode(String query, Integer size, List<String> expect)
            throws Exception
    {
        assertFunction(query, new ArrayType(VarcharType.createVarcharType(size)), expect);
    }

    @DataProvider(name = "query-provider-dict")
    public Object[][] dataProvider3()
    {
        return new Object[][] {
                {"kuromoji_tokenize('一風堂のラーメンが食べたい。', 'normal', ARRAY['一風堂,一風堂,イップウドウ,名詞', '一蘭,一蘭,イチラン,名詞'] )", 14,
                 ImmutableList.of("一風堂", "ラーメン", "食べる")},
                };
    }

    @Test(invocationCount = 10, dataProvider = "query-provider-dict")
    public void testKuromojiNormalWithMandD(String query, Integer size, List<String> expect)
            throws Exception
    {
        assertFunction(query, new ArrayType(VarcharType.createVarcharType(size)), expect);
    }

    @Test
    public void testKuromojiEmpty()
            throws Exception
    {
        assertFunction("kuromoji_tokenize('')",
                new ArrayType(VarcharType.createVarcharType(0)),
                ImmutableList.of());
    }

    @Test
    public void testInvalidArgument()
    {
        assertInvalidFunction("kuromoji_tokenize('', 'failValue')",
                "Invalid mode value: failValue");
    }
}

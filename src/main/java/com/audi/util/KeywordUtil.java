package com.audi.util;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * User : LinShiYue
 * Date : 2023-12-21 18:47:18
 * Description :
 */
public class KeywordUtil {
    public static List<String> splitKeyWord(String keyWord) throws IOException {

        //创建集合用于存放切分后的数据
        ArrayList<String> list = new ArrayList<>();

        //创建IK分词对象 ik_smart
        StringReader stringReader = new StringReader(keyWord);

        IKSegmenter ikSegmenter = new IKSegmenter(stringReader, false);

        //取出切好的词
        Lexeme next = ikSegmenter.next();
        while (next != null) {
            String word = next.getLexemeText();
            list.add(word);
            next = ikSegmenter.next();
        }

        return list;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(splitKeyWord("尚硅谷大数据项目Flink实时"));
    }
}

package app;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// TODO https://ja.wikipedia.org/wiki/Unicode


// TODO https://www.utf8-chartable.de/unicode-utf8-table.pl?utf8=dec 157 ~ 159あたりは制御コードなので、range指定時は飛ばす

//作り方
//#1. Const Define
//#2. Function Define
//#3. Option-Function Define

//TODO
//19. range-onlyの際のmin-cp:max-cpのレスが遅過ぎ

public class App {

    private static final String DEFAULT_NONE_KEYWORD="ウンコもりもり森鴎外";

    static BiFunction<Integer,Integer,Map<Integer,String>> mkInputUnicodeName = (s,e) -> {
        return IntStream.rangeClosed(s,e).boxed().parallel().collect(Collectors.toMap(i->i,i->Optional.ofNullable(cpToUnicodeName(i)).orElse(DEFAULT_NONE_KEYWORD))).entrySet().stream()
                .collect(Collectors.toMap(ee->ee.getKey(),ee->ee.getValue()));
    };
    static BiFunction<Integer,Integer,Map<Integer,String>> mkInputUnicodeScriptName = (s,e) -> {
        return IntStream.rangeClosed(s,e).boxed().parallel().collect(Collectors.toMap(i->i,i->Optional.ofNullable(cpToUnicodeScriptName(i)).orElse(DEFAULT_NONE_KEYWORD))).entrySet().stream()
                .collect(Collectors.toMap(ee->ee.getKey(),ee->ee.getValue()));
    };
    static BiFunction<Integer,Integer,Map<Integer,String>> mkInputUnicodeBlockName = (s,e) -> {
        return IntStream.rangeClosed(s,e).boxed().parallel().collect(Collectors.toMap(i->i,i->Optional.ofNullable(cpToUnicodeBlockName(i)).orElse(DEFAULT_NONE_KEYWORD))).entrySet().stream()
                .collect(Collectors.toMap(ee->ee.getKey(),ee->ee.getValue()));
    };
    static Function<Integer, String> cpToStr = (n)-> {
        return new String(Character.toChars(n));
    };
    static Function<Integer, String> numToStr = (n)-> {
        return String.valueOf(n);
    };
    static BiFunction<String, Normalizer.Form, String> strToNorm = (s,typ)-> {
        return Normalizer.normalize(s,typ);
    };
    static Function<Integer, String> cpToUnicodeScriptName = (n)-> {
        return Character.UnicodeScript.of(n).name();
    };
    static Function<Integer, String> cpToUnicodeBlockName = (n)-> {
        return String.valueOf(Character.UnicodeBlock.of(n));
    };
    static Function<Integer, String> cpToUnicodeName = (n)-> {
        return Character.getName(n);
    };
    static Function<String, String> strToUtf8 = (s)-> {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        Pattern p = Pattern.compile("^1*0");
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<b.length;i++){
            String bin = hexToBin(String.format("%02X",b[i]));
            Matcher mc = p.matcher(bin);
            if(mc.find()){
                if(2!=mc.group().length()){
                    sb.append("\n"+binToHex(bin));
                }else{
                    sb.append(binToHex(bin));
                }
            }
        }
        return Stream.of(sb.toString()).flatMap(e-> Arrays.stream(e.split("\n"))).filter(ee->0!=ee.length()).collect(Collectors.joining("-"));
    };
    static Function<String, String> strToUtf16 = (s)-> {
        byte[] b = s.getBytes(StandardCharsets.UTF_16);
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<b.length;i++){
            String hex = String.format("%02X",b[i]);
            if(hex.equals("FE")||hex.equals("FF")){
            }else if((sb.length()-sb.toString().split("-").length+1)%4==0&&0<sb.length()){
                sb.append("-"+hex);
            }
            else{
                sb.append(hex);
            }
        }
        return sb.toString();
    };
    static Function<String, String> strToUtf32 = (s)-> {
        byte[] b = s.getBytes(Charset.forName("UTF-32"));
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<b.length;i++){
            String hex = String.format("%02X",b[i]);
            if((sb.length()-sb.toString().split("-").length+1)%8==0&&0<sb.length()) {
                sb.append("-" + hex);
            }else{
                sb.append(hex);
            }
        }
        return sb.toString();
    };
    static Function<String, String> strToUnicode = (s)-> {
        return IntStream.range(0,s.length()).boxed().map(e->String.format("U+%05X",(int)s.charAt(e))).collect(Collectors.joining("-"));
    };
    static BiFunction<Map<Integer, String>,Integer,Map<String, List<String>>> mkWordIdxWordSplit = (m,n) -> {
        Map<String, String> tmp = new HashMap<>();
        for(Map.Entry<Integer, String> entry : m.entrySet()){
            List<String> l = Arrays.asList(entry.getValue().split("\\W")).stream().collect(Collectors.toList());
            int mx = l.size();
            for(int i=0;i<mx;i++){
                tmp.put(entry.getKey() +"-"+ i,l.get(i));
            }
        }
        return tmp.entrySet().stream().collect(Collectors.groupingBy(e->e.getValue(),Collectors.mapping(e->e.getKey(),Collectors.toList())));
    };
    static BiFunction<Map<Integer, String>,Integer,Map<String, List<String>>> mkWordIdxWordHyphenSplit = (m,n) -> {
        Map<String, String> tmp = new HashMap<>();
        for(Map.Entry<Integer, String> entry : m.entrySet()){
            List<String> l = Arrays.asList(entry.getValue().split("\\W")).stream().flatMap(e->Arrays.asList(e.split("-")).stream()).collect(Collectors.toList());
            int mx = l.size();
            for(int i=0;i<mx;i++){
                tmp.put(entry.getKey() +"-"+ i,l.get(i));
            }
        }
        return tmp.entrySet().stream().collect(Collectors.groupingBy(e->e.getValue(),Collectors.mapping(e->e.getKey(),Collectors.toList())));
    };
    static BiFunction<Map<Integer, String>,Integer,Map<String, List<String>>> mkWordIdxWordUnderScoreSplit = (m,n) -> {
        Map<String, String> tmp = new HashMap<>();
        for(Map.Entry<Integer, String> entry : m.entrySet()){
            List<String> l = Arrays.asList(entry.getValue().split("\\W")).stream().flatMap(e->Arrays.asList(e.split("_")).stream()).collect(Collectors.toList());
            int mx = l.size();
            for(int i=0;i<mx;i++){
                tmp.put(entry.getKey() +"-"+ i,l.get(i));
            }
        }
        return tmp.entrySet().stream().collect(Collectors.groupingBy(e->e.getValue(),Collectors.mapping(e->e.getKey(),Collectors.toList())));
    };
    static BiFunction<Map<Integer, String>,Integer,Map<String, List<String>>> mkWordIdxWordUnderScoreHyphenSplit = (m,n) -> {
        Map<String, String> tmp = new HashMap<>();
        for(Map.Entry<Integer, String> entry : m.entrySet()){
            List<String> l = Arrays.asList(entry.getValue().split("\\W")).stream().flatMap(e->Arrays.asList(e.split("_")).stream()).flatMap(e->Arrays.asList(e.split("-")).stream()).collect(Collectors.toList());
            int mx = l.size();
            for(int i=0;i<mx;i++){
                tmp.put(entry.getKey() +"-"+ i,l.get(i));
            }
        }
        return tmp.entrySet().stream().collect(Collectors.groupingBy(e->e.getValue(),Collectors.mapping(e->e.getKey(),Collectors.toList())));
    };
    static BiFunction<Map<Integer, String>,Integer,Map<String, List<String>>> mkNgramIdxWordSplit = (m,n) -> {
        Map<String, String> tmp = new HashMap<>();
        for(Map.Entry<Integer, String> entry : m.entrySet()){
            List<String> l = Arrays.asList(entry.getValue().split("\\W"));
            int mx = l.size();
            for(int i=0;i<mx;i++){
                List<String> ll = ngram(l.get(i),n);
                int mxmx=ll.size();
                for(int j=0;j<mxmx;j++){
                    tmp.put(entry.getKey() +"-"+ i+"-"+ j,ll.get(j));
                }
            }
        }
        return tmp.entrySet().stream().collect(Collectors.groupingBy(e->e.getValue(),Collectors.mapping(e->e.getKey(),Collectors.toList())));
    };
    static BiFunction<Map<Integer, String>,Integer,Map<String, List<String>>> mkNgramIdxWordHyphenSplit = (m,n) -> {
        Map<String, String> tmp = new HashMap<>();
        for(Map.Entry<Integer, String> entry : m.entrySet()){
            List<String> l = Arrays.asList(entry.getValue().split("\\W")).stream().flatMap(e->Arrays.asList(e.split("-")).stream()).collect(Collectors.toList());
            int mx = l.size();
            for(int i=0;i<mx;i++){
                List<String> ll = ngram(l.get(i),n);
                int mxmx=ll.size();
                for(int j=0;j<mxmx;j++){
                    tmp.put(entry.getKey() +"-"+ i+"-"+ j,ll.get(j));
                }
            }
        }
        return tmp.entrySet().stream().collect(Collectors.groupingBy(e->e.getValue(),Collectors.mapping(e->e.getKey(),Collectors.toList())));
    };
    static BiFunction<Map<Integer, String>,Integer,Map<String, List<String>>> mkNgramIdxWordUnderScoreSplit = (m,n) -> {
        Map<String, String> tmp = new HashMap<>();
        for(Map.Entry<Integer, String> entry : m.entrySet()){
            List<String> l = Arrays.asList(entry.getValue().split("\\W")).stream().flatMap(e->Arrays.asList(e.split("_")).stream()).collect(Collectors.toList());
            int mx = l.size();
            for(int i=0;i<mx;i++){
                List<String> ll = ngram(l.get(i),n);
                int mxmx=ll.size();
                for(int j=0;j<mxmx;j++){
                    tmp.put(entry.getKey() +"-"+ i+"-"+ j,ll.get(j));
                }
            }
        }
        return tmp.entrySet().stream().collect(Collectors.groupingBy(e->e.getValue(),Collectors.mapping(e->e.getKey(),Collectors.toList())));
    };
    static BiFunction<Map<Integer, String>,Integer,Map<String, List<String>>> mkNgramIdxWordUnderScoreHyphenSplit = (m,n) -> {
        Map<String, String> tmp = new HashMap<>();
        for(Map.Entry<Integer, String> entry : m.entrySet()){
            List<String> l = Arrays.asList(entry.getValue().split("\\W")).stream().flatMap(e->Arrays.asList(e.split("_")).stream()).flatMap(e->Arrays.asList(e.split("-")).stream()).collect(Collectors.toList());
            int mx = l.size();
            for(int i=0;i<mx;i++){
                List<String> ll = ngram(l.get(i),n);
                int mxmx=ll.size();
                for(int j=0;j<mxmx;j++){
                    tmp.put(entry.getKey() +"-"+ i+"-"+ j,ll.get(j));
                }
            }
        }
        return tmp.entrySet().stream().collect(Collectors.groupingBy(e->e.getValue(),Collectors.mapping(e->e.getKey(),Collectors.toList())));
    };
    static Function<List<String>,Set<Integer>> mkIdxShape = (l) -> {
        return l.stream().map(ee->Integer.valueOf(ee.substring(0,ee.indexOf("-")==-1?ee.length():ee.indexOf("-")))).collect(Collectors.toSet());
    };
    static BiFunction<Map<Integer,String>,String,Set<Integer>> mkIdxFilter = (m,s) -> {
        return m.entrySet().stream()
                .filter(v->v.getValue().contains(s)).map(ee->ee.getKey()).collect(Collectors.toSet());
    };

    private static final String PROGRAM_NAME = "unidat";
    private static final String PROGRAM_VERSION = "1-0-0";

    private static final Integer SUCCESS_STATUS=0;
    private static final Integer FAILURE_STATUS=1;

    private static Integer SEQ_CNT=0;
    private static Integer GRP_CNT=0;
    private static final String ON = "1";
    private static final String OFF = "0";
    private static final String ARGS_SEPARATOR = ":";

    private static final String OPTION_IDX_INPUT_PATTERN="IDX_INPUT_PATTERN";
    private static final String OPTION_SEARCH_KEYWORD="SEARCH_KEYWORD";

    private static Integer DEFAULT_START_RN =Character.MIN_CODE_POINT;
    private static Integer DEFAULT_END_RN=Character.MAX_CODE_POINT;

    private static final String OPTION_RANGE = "OPTION_RANGE";
    private static final String OPTION_HELP = "OPTION_HELP";
    private static final String OPTION_VERSION = "OPTION_VERSION";
    private static final String OPTION_USAGE = "OPTION_USAGE";

    //grouping key
    private static final String OPTION_WORD_SEARCH = "WORD_SEARCH";
    private static final String OPTION_NGRAM_SEARCH = "NGRAM_SEARCH";
    private static final String OPTION_HASH_KEY_SEARCH = "HASH_KEY_SEARCH";

    private static final String OPTION_IDX_INPUT_UNICODE_NAME = "IDX_INPUT_UNICODE_NAME";
    private static final String OPTION_IDX_INPUT_UNICODE_SCRIPT_NAME = "IDX_INPUT_UNICODE_SCRIPT_NAME";
    private static final String OPTION_IDX_INPUT_UNICODE_BLOCK_NAME = "IDX_INPUT_UNICODE_BLOCK_NAME";

    private static final String SEQ = "SEQ";
    private static final String GRP = "GRP";
    private static final String GRPSEQ ="GRPSEQ";

    private static final String OPTION_NUM_TO_STR="NUM_TO_STR";
    private static final String OPTION_CP_TO_STR="CP_TO_STR";
    private static final String OPTION_CP_TO_UNICODE_NAME="CP_TO_UNICODE_NAME";
    private static final String OPTION_CP_TO_UNICODE_SCRIPT_NAME="CP_TO_UNICODE_SCRIPT_NAME";
    private static final String OPTION_CP_TO_UNICODE_BLOCK_NAME="CP_TO_UNICODE_BLOCK_NAME";
    private static final String OPTION_STR_TO_UTF8="STR_TO_UTF8";
    private static final String OPTION_STR_TO_UTF16="STR_TO_UTF16";
    private static final String OPTION_STR_TO_UTF32="STR_TO_UTF32";
    private static final String OPTION_STR_TO_UNICODE="STR_TO_UNICODE";
    private static final String OPTION_STR_TO_NORM="STR_TO_NORM";

    private static final String OPTION_NORM_GRP_NFC="NORM_GRP_NFC";
    private static final String OPTION_NORM_GRP_NFD="NORM_GRP_NFD";
    private static final String OPTION_NORM_GRP_NFKC="NORM_GRP_NFKC";
    private static final String OPTION_NORM_GRP_NFKD="NORM_GRP_NFKD";


    private static final String OPTION_MK_WORD_IDX_NON_SPLIT="MK_WORD_IDX_NON_SPLIT";
    private static final String OPTION_MK_WORD_IDX_WORD_SPLIT="MK_WORD_IDX_WORD_SPLIT";
    private static final String OPTION_MK_WORD_IDX_WORD_HYPHEN_SPLIT="MK_WORD_IDX_WORD_HYPHEN_SPLIT";
    private static final String OPTION_MK_WORD_IDX_WORD_UNDERSCORE_SPLIT="MK_WORD_IDX_WORD_UNDERSCORE_SPLIT";
    private static final String OPTION_MK_WORD_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT="MK_WORD_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT";
    private static final String OPTION_MK_NGRAM_IDX_NON_SPLIT="MK_NGRAM_IDX_NON_SPLIT";
    private static final String OPTION_MK_NGRAM_IDX_WORD_SPLIT="MK_NGRAM_IDX_WORD_SPLIT";
    private static final String OPTION_MK_NGRAM_IDX_WORD_HYPHEN_SPLIT="MK_NGRAM_IDX_WORD_HYPHEN_SPLIT";
    private static final String OPTION_MK_NGRAM_IDX_WORD_UNDERSCORE_SPLIT="MK_NGRAM_IDX_WORD_UNDERSCORE_SPLIT";
    private static final String OPTION_MK_NGRAM_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT="MK_NGRAM_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT";

    private static final String OPTION_MK_IDX_SHAPE="MK_IDX_SHAPE";
    private static final String OPTION_MK_IDX_FILTER="MK_IDX_FILTER";

    private static final String OPTION_SEARCH_KEYWORD_PATTERN="[A-Z]+";

    private static final Map<String, List<String>> OPTION_ARGS_PATTERN = new LinkedHashMap<>(){{
        put(OPTION_NUM_TO_STR, Arrays.asList("-cp", "--cp", "--codepoint"));
        put(OPTION_CP_TO_STR, Arrays.asList("-s", "-str", "--str"));
        put(OPTION_CP_TO_UNICODE_NAME, Arrays.asList("-unm", "--unm"));
        put(OPTION_CP_TO_UNICODE_SCRIPT_NAME, Arrays.asList("-usc", "--usc"));
        put(OPTION_CP_TO_UNICODE_BLOCK_NAME, Arrays.asList("-ubl", "--ubl"));
        put(OPTION_STR_TO_UTF8, Arrays.asList("-u8", "--u8"));
        put(OPTION_STR_TO_UTF16, Arrays.asList("-u16", "--u16"));
        put(OPTION_STR_TO_UTF32, Arrays.asList("-u32","--u32"));
        put(OPTION_STR_TO_UNICODE, Arrays.asList("-unicode", "--unicode"));
        put(OPTION_NORM_GRP_NFC, Arrays.asList("-nfc", "--nfc"));
        put(OPTION_NORM_GRP_NFD, Arrays.asList("-nfd","--nfd"));
        put(OPTION_NORM_GRP_NFKC, Arrays.asList("-nfkc", "--nfkc"));
        put(OPTION_NORM_GRP_NFKD, Arrays.asList("-nfkd", "--nfkd"));

        put(OPTION_IDX_INPUT_UNICODE_NAME, Arrays.asList("-input-unicode-name", "--input-unicode-name"));
        put(OPTION_IDX_INPUT_UNICODE_SCRIPT_NAME, Arrays.asList("-input-unicode-script-name", "--input-unicode-script-name"));
        put(OPTION_IDX_INPUT_UNICODE_BLOCK_NAME, Arrays.asList("-input-unicode-block-name","--input-unicode-block-name"));

        put(OPTION_MK_WORD_IDX_NON_SPLIT, Arrays.asList("-word-nonsplit", "--word-nonsplit"));
        put(OPTION_MK_WORD_IDX_WORD_SPLIT, Arrays.asList("-word-split", "--word-split"));
        put(OPTION_MK_WORD_IDX_WORD_HYPHEN_SPLIT, Arrays.asList("-word-hyphen-split","--word-hyphen-split"));
        put(OPTION_MK_WORD_IDX_WORD_UNDERSCORE_SPLIT, Arrays.asList("-word-underscore-split", "--word-underscore-split"));
        put(OPTION_MK_WORD_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT, Arrays.asList("-word-all-split", "--word-all-split"));
        put(OPTION_MK_NGRAM_IDX_NON_SPLIT, Arrays.asList("-ngram-nonsplit", "--ngram-nonsplit"));
        put(OPTION_MK_NGRAM_IDX_WORD_SPLIT, Arrays.asList("-ngram-split", "--ngram-split"));
        put(OPTION_MK_NGRAM_IDX_WORD_HYPHEN_SPLIT, Arrays.asList("-ngram-hyphen-split","--ngram-hyphen-split"));
        put(OPTION_MK_NGRAM_IDX_WORD_UNDERSCORE_SPLIT, Arrays.asList("-ngram-underscore-split", "--ngram-underscore-split"));
        put(OPTION_MK_NGRAM_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT, Arrays.asList("-ngram-all-split", "--ngram-all-split"));
        put(OPTION_HASH_KEY_SEARCH, Arrays.asList("-hash", "--hash", "-hashkey", "-hashKey", "-HashKey", "-Hashkey", "--hashkey", "--hashKey", "--HashKey", "--Hashkey"));

        put(OPTION_RANGE, Arrays.asList("-r.*", "--r.*", "--range.*", "-range.*"));
        put(OPTION_HELP, Arrays.asList("-h", "--h", "--help", "-help"));
        put(OPTION_VERSION, Arrays.asList("-v", "--v", "-V", "--V", "-version", "--version"));
        put(OPTION_USAGE, Arrays.asList("-usage", "--usage", "-Usage", "--Usage"));

    }};
    //正規化パタンを定義
    private static final Map<String, Normalizer.Form> NORMALIZE_PATTERN_MAP = new HashMap<>(){{
        put(OPTION_NORM_GRP_NFC, Normalizer.Form.NFC);
        put(OPTION_NORM_GRP_NFD, Normalizer.Form.NFD);
        put(OPTION_NORM_GRP_NFKC, Normalizer.Form.NFKC);
        put(OPTION_NORM_GRP_NFKD, Normalizer.Form.NFKD);
    }};
    //グループ化対象キーを定義
    private static final List<String> grpArgsList = new ArrayList<>(Arrays.asList(
            OPTION_MK_WORD_IDX_NON_SPLIT
            ,OPTION_MK_WORD_IDX_WORD_SPLIT
            ,OPTION_MK_WORD_IDX_WORD_HYPHEN_SPLIT
            ,OPTION_MK_WORD_IDX_WORD_UNDERSCORE_SPLIT
            ,OPTION_MK_WORD_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT

            ,OPTION_MK_NGRAM_IDX_NON_SPLIT
            ,OPTION_MK_NGRAM_IDX_WORD_SPLIT
            ,OPTION_MK_NGRAM_IDX_WORD_HYPHEN_SPLIT
            ,OPTION_MK_NGRAM_IDX_WORD_UNDERSCORE_SPLIT
            ,OPTION_MK_NGRAM_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT

            ,OPTION_HASH_KEY_SEARCH

            ,OPTION_IDX_INPUT_UNICODE_NAME
            ,OPTION_IDX_INPUT_UNICODE_SCRIPT_NAME
            ,OPTION_IDX_INPUT_UNICODE_BLOCK_NAME

            ,OPTION_SEARCH_KEYWORD
    ));
    //取得対象列リストを定義
    private static final List<String> COL_ARGS_LIST = new ArrayList<>(Arrays.asList(
            SEQ
            ,GRP
            ,GRPSEQ
            ,OPTION_NUM_TO_STR
            ,OPTION_CP_TO_STR
            ,OPTION_CP_TO_UNICODE_NAME
            ,OPTION_CP_TO_UNICODE_SCRIPT_NAME
            ,OPTION_CP_TO_UNICODE_BLOCK_NAME
            ,OPTION_STR_TO_UTF8
            ,OPTION_STR_TO_UTF16
            ,OPTION_STR_TO_UTF32
            ,OPTION_STR_TO_UNICODE
            ,OPTION_NORM_GRP_NFC
            ,OPTION_NORM_GRP_NFD
            ,OPTION_NORM_GRP_NFKC
            ,OPTION_NORM_GRP_NFKD
    ));

    //単語分割メソッドリストを定義
    private static final List<String> WORD_SPLIT_LIST = new ArrayList<>(Arrays.asList(
            OPTION_MK_WORD_IDX_NON_SPLIT
            ,OPTION_MK_WORD_IDX_WORD_SPLIT
            ,OPTION_MK_WORD_IDX_WORD_HYPHEN_SPLIT
            ,OPTION_MK_WORD_IDX_WORD_UNDERSCORE_SPLIT
            ,OPTION_MK_WORD_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT
    ));
    //NGRAM分割メソッドリストを定義
    private static final List<String> NGRAM_SPLIT_LIST = new ArrayList<>(Arrays.asList(
            OPTION_MK_NGRAM_IDX_NON_SPLIT
            ,OPTION_MK_NGRAM_IDX_WORD_SPLIT
            ,OPTION_MK_NGRAM_IDX_WORD_HYPHEN_SPLIT
            ,OPTION_MK_NGRAM_IDX_WORD_UNDERSCORE_SPLIT
            ,OPTION_MK_NGRAM_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT
    ));
    //引数に指定されたら設定するマップ
    private static final Map<String, Map<String,List<String>>> OPTION_FLG_PATTERN = new HashMap<>(){{
        //ONするマップ
        put(ON, new LinkedHashMap<>(){{
            put(OPTION_HELP,Arrays.asList(ON));
            put(OPTION_VERSION,Arrays.asList(ON));
            put(OPTION_USAGE,Arrays.asList(ON));
            put(OPTION_RANGE,Arrays.asList(ON));
            put(OPTION_MK_WORD_IDX_NON_SPLIT,Arrays.asList(ON));
            put(OPTION_MK_WORD_IDX_WORD_SPLIT,Arrays.asList(ON));
            put(OPTION_MK_WORD_IDX_WORD_HYPHEN_SPLIT,Arrays.asList(ON));
            put(OPTION_MK_WORD_IDX_WORD_UNDERSCORE_SPLIT,Arrays.asList(ON));
            put(OPTION_MK_WORD_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT,Arrays.asList(ON));
            put(OPTION_MK_NGRAM_IDX_NON_SPLIT,Arrays.asList(ON));
            put(OPTION_MK_NGRAM_IDX_WORD_SPLIT,Arrays.asList(ON));
            put(OPTION_MK_NGRAM_IDX_WORD_HYPHEN_SPLIT,Arrays.asList(ON));
            put(OPTION_MK_NGRAM_IDX_WORD_UNDERSCORE_SPLIT,Arrays.asList(ON));
            put(OPTION_MK_NGRAM_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT,Arrays.asList(ON));
            put(OPTION_IDX_INPUT_UNICODE_NAME,Arrays.asList(ON));
            put(OPTION_IDX_INPUT_UNICODE_SCRIPT_NAME,Arrays.asList(ON));
            put(OPTION_IDX_INPUT_UNICODE_BLOCK_NAME,Arrays.asList(ON));
            put(OPTION_HASH_KEY_SEARCH,Arrays.asList(ON));
            put(OPTION_SEARCH_KEYWORD, Arrays.asList(OPTION_SEARCH_KEYWORD_PATTERN));
        }});
        //OFFするマップ
        put(OFF, new LinkedHashMap<>(){{
            put(OPTION_NUM_TO_STR,Arrays.asList(OFF));
            put(OPTION_CP_TO_STR,Arrays.asList(OFF));
            put(OPTION_CP_TO_UNICODE_NAME,Arrays.asList(OFF));
            put(OPTION_CP_TO_UNICODE_SCRIPT_NAME,Arrays.asList(OFF));
            put(OPTION_CP_TO_UNICODE_BLOCK_NAME,Arrays.asList(OFF));
            put(OPTION_STR_TO_UTF8,Arrays.asList(OFF));
            put(OPTION_STR_TO_UTF16,Arrays.asList(OFF));
            put(OPTION_STR_TO_UTF32,Arrays.asList(OFF));
            put(OPTION_STR_TO_UNICODE,Arrays.asList(OFF));
            put(OPTION_NORM_GRP_NFC,Arrays.asList(OFF));
            put(OPTION_NORM_GRP_NFD,Arrays.asList(OFF));
            put(OPTION_NORM_GRP_NFKC,Arrays.asList(OFF));
            put(OPTION_NORM_GRP_NFKD,Arrays.asList(OFF));
        }});
    }};
    //引数に指定されなかったら設定するマップ
    private static final Map<String, Map<String,List<String>>> OPTION_DEFAULT_FLG_PATTERN = new HashMap<>(){{
        //ONするマップ
        put(ON, new LinkedHashMap<>(){{

            //デフォルトでマスト列
            put(SEQ,Arrays.asList(ON));
            put(GRP,Arrays.asList(ON));
            put(GRPSEQ,Arrays.asList(ON));




            put(OPTION_NUM_TO_STR,Arrays.asList(ON));
            put(OPTION_CP_TO_STR,Arrays.asList(ON));
            put(OPTION_CP_TO_UNICODE_NAME,Arrays.asList(ON));
            put(OPTION_CP_TO_UNICODE_SCRIPT_NAME,Arrays.asList(ON));
            put(OPTION_CP_TO_UNICODE_BLOCK_NAME,Arrays.asList(ON));
            put(OPTION_STR_TO_UTF8,Arrays.asList(ON));
            put(OPTION_STR_TO_UTF16,Arrays.asList(ON));
            put(OPTION_STR_TO_UTF32,Arrays.asList(ON));
            put(OPTION_STR_TO_UNICODE,Arrays.asList(ON));
            put(OPTION_NORM_GRP_NFC,Arrays.asList(ON));
            put(OPTION_NORM_GRP_NFD,Arrays.asList(ON));
            put(OPTION_NORM_GRP_NFKC,Arrays.asList(ON));
            put(OPTION_NORM_GRP_NFKD,Arrays.asList(ON));
        }});
        //OFFするマップ
        put(OFF, new LinkedHashMap<>(){{
            put(OPTION_HELP,Arrays.asList(OFF));
            put(OPTION_VERSION,Arrays.asList(OFF));
            put(OPTION_USAGE,Arrays.asList(OFF));
        }});
    }};
    private static final Map<String,BiFunction<Integer,Integer,Map<Integer,String>>> MK_INPUT_FUNCTION_MAP = new LinkedHashMap<>(){{
        put(OPTION_IDX_INPUT_UNICODE_NAME, mkInputUnicodeName);
        put(OPTION_IDX_INPUT_UNICODE_SCRIPT_NAME, mkInputUnicodeScriptName);
        put(OPTION_IDX_INPUT_UNICODE_BLOCK_NAME, mkInputUnicodeBlockName);
    }};
    private static final Map<String,BiFunction<Map<Integer, String>,Integer,Map<String, List<String>>>> SPLIT_PROCESS_FUNCTION_MAP = new LinkedHashMap<>(){{
        put(OPTION_MK_WORD_IDX_WORD_SPLIT,mkWordIdxWordSplit);
        put(OPTION_MK_WORD_IDX_WORD_HYPHEN_SPLIT,mkWordIdxWordHyphenSplit);
        put(OPTION_MK_WORD_IDX_WORD_UNDERSCORE_SPLIT,mkWordIdxWordUnderScoreSplit);
        put(OPTION_MK_WORD_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT,mkWordIdxWordUnderScoreHyphenSplit);
        put(OPTION_MK_NGRAM_IDX_WORD_SPLIT,mkNgramIdxWordSplit);
        put(OPTION_MK_NGRAM_IDX_WORD_HYPHEN_SPLIT,mkNgramIdxWordHyphenSplit);
        put(OPTION_MK_NGRAM_IDX_WORD_UNDERSCORE_SPLIT,mkNgramIdxWordUnderScoreSplit);
        put(OPTION_MK_NGRAM_IDX_WORD_UNDERSCORE_HYPHEN_SPLIT,mkNgramIdxWordUnderScoreHyphenSplit);
    }};
    private static final Map<String,Function<List<String>,Set<Integer>>> SHAPE_PROCESS_FUNCTION_MAP = new LinkedHashMap<>(){{
        put(OPTION_MK_IDX_SHAPE, mkIdxShape);
    }};
    private static final Map<String,BiFunction<Map<Integer,String>,String,Set<Integer>>> FILTER_PROCESS_FUNCTION_MAP = new LinkedHashMap<>(){{
        put(OPTION_MK_IDX_FILTER, mkIdxFilter);
    }};

    private static final Map<String,Function<Integer,String>> SINGLE_ARGS_FUNCTION_IN_NUM_OUT_STR_MAP = new LinkedHashMap<>(){{
        put(OPTION_NUM_TO_STR, numToStr);
        put(OPTION_CP_TO_STR, cpToStr);
        put(OPTION_CP_TO_UNICODE_NAME, cpToUnicodeName);
        put(OPTION_CP_TO_UNICODE_SCRIPT_NAME, cpToUnicodeScriptName);
        put(OPTION_CP_TO_UNICODE_BLOCK_NAME, cpToUnicodeBlockName);
    }};
    private static final Map<String,Function<String,String>> SINGLE_ARGS_FUNCTION_IN_STR_OUT_STR_MAP = new LinkedHashMap<>(){{
        put(OPTION_STR_TO_UTF8, strToUtf8);
        put(OPTION_STR_TO_UTF16, strToUtf16);
        put(OPTION_STR_TO_UTF32, strToUtf32);
        put(OPTION_STR_TO_UNICODE, strToUnicode);
    }};
    private static final Map<String,BiFunction<String,Normalizer.Form,String>> MULTIPLE_ARGS_FUNCTION_MAP = new LinkedHashMap<>(){{
        put(OPTION_STR_TO_NORM, strToNorm);
    }};

    private static void optionUsage(Integer status,String... optionPtn){
        for(String option : optionPtn){
            switch (option){
                case OPTION_HELP:
                case OPTION_USAGE:
                    optionHelp();
                    break;
                case OPTION_VERSION:
                    optionVersion();
                    break;
                case OPTION_WORD_SEARCH:
                    usageWordSearch();
                    break;
                case OPTION_NGRAM_SEARCH:
                    usageNgramSearch();
                    break;
                case OPTION_HASH_KEY_SEARCH:
                    usageHashKeySearch();
                    break;
                case OPTION_IDX_INPUT_PATTERN:
                    optionHelp();
                    break;
                default:
                    break;
            }
        }
        System.exit(status);
    }
    private static void optionHelp(){
        System.out.println(PROGRAM_NAME + " --range:1:30"); //レンジを絞って出力
        System.out.println(PROGRAM_NAME + " --range:50:80 -nfc -nfd"); //レンジ絞ってサプレスして出力
        System.out.println(PROGRAM_NAME + " --range:12354:12390 -nfc -nfd -nfkc"); //レンジ絞ってサプレスして出力
        System.out.println(PROGRAM_NAME + " --range:12354:12390 -cp -usc -ubl -u8 -u32 --unicode"); //レンジ絞ってサプレスして出力
        System.out.println(PROGRAM_NAME + " -input-unicode-name -word-split HIRAGANA");
        System.out.println(PROGRAM_NAME + " -input-unicode-name -word-split HIRAGANA -input-unicode-block-name -ngram-hyphen-split HAN -nfc -nfd -nfkc");
        System.out.println(PROGRAM_NAME + " -input-unicode-name -word-split HIRAGANA -input-unicode-name -word-split KATAKANA -input-unicode-name -word-split HIRAGANA  -cp -usc -ubl -u8 -u32 --unicode");
        System.out.println(PROGRAM_NAME + " -input-unicode-name --hashkey HIRAGANA  -input-unicode-name --hashkey HIRAGANA");
        System.out.println(PROGRAM_NAME + " -input-unicode-script-name --hash KATAKANA -input-unicode-name --hashkey HIRAGANA -nfc -nfd -nfkc");
        System.out.println(PROGRAM_NAME + " -nfc -nfd -nfkc -input-unicode-block-name --hashkey HIRAGANA -input-unicode-script-name --hash KATAKANA");
        System.out.println(PROGRAM_NAME + " -nfc -nfd -nfkc -input-unicode-script-name --hash KATAKANA -nfc -nfd -nfkc -input-unicode-block-name --hashkey HIRAGANA");
        System.out.println(PROGRAM_NAME + " -input-unicode-block-name -word-underscore-split MISCELLANEOUS -u32 --nfd -nfc -nfkc -u16 -u32 -unicode");
        System.out.println(PROGRAM_NAME + " -input-unicode-block-name -word-underscore-split PICTOGRAPHS -u32 --nfd -nfc -nfkc -u16 -u32 -unicode");
        System.out.println(PROGRAM_NAME + " -input-unicode-name -word-split MOYAI -u32 --nfd -nfc -nfkc -u16 -u32 -unicode");
        System.out.println(PROGRAM_NAME + " -input-unicode-name -word-split SILHOUETTE -u32 --nfd -nfc -nfkc -u16 -u32");
        System.out.println(PROGRAM_NAME + " -input-unicode-name -word-split SILHOUETTE -u32 --nfd -nfc -nfkc -u16 -u32  -input-unicode-name -word-split MOYAI -u32 --nfd -nfc -nfkc -u16 -u32 -unicode");
    }
    private static void optionVersion(){
        System.out.println(PROGRAM_VERSION);
    }
    private static void usageWordSearch(){
        final String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        System.out.println(methodName);
    }
    private static void usageNgramSearch(){
        final String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        System.out.println(methodName);
    }
    private static void usageHashKeySearch(){
        final String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        System.out.println(methodName);
    }
    private static String hexToBin(String s){
        return Integer.toBinaryString(Integer.parseInt(s,16));
    }
    private static String binToHex(String s){
        return Integer.toHexString(Integer.parseInt(s,2));
    }
    private static String cpToUnicodeName(Integer n){
        return Character.getName(n);
    }
    private static String cpToUnicodeScriptName(Integer n){
        return Character.UnicodeScript.of(n).name();
    }
    private static String cpToUnicodeBlockName(Integer n){
        return String.valueOf(Character.UnicodeBlock.of(n));
    }
    private static <N,S> Map<N, List<S>> executeMkTbl(
            N seq
            ,N grp
            ,N grpSeq
            ,N i
            ,Map<S,Function<N,S>> singleArgFunctionInNumOutStrMap
            ,Map<S,Function<S,S>> singleArgFunctionInStrOutStrMap
            ,Map<S,BiFunction<S,Normalizer.Form,S>> multipleArgFunctionMap
            ,Map<S,List<S>> suppressColumnsMap
    ){

        Map<N, List<S>> rt = new LinkedHashMap<>();

        Function<N,S> numToStr = singleArgFunctionInNumOutStrMap.get(OPTION_NUM_TO_STR); //numToStr(i)
        Function<N,S> cpToStr = singleArgFunctionInNumOutStrMap.get(OPTION_CP_TO_STR); //cpToStr(i)

        BiFunction<S,Normalizer.Form,S> strToNorm = multipleArgFunctionMap.get(OPTION_STR_TO_NORM);

        // Must Item
        //grp,grpseq
        List<S> l = new ArrayList<>();
        l.add(numToStr.apply(grp));
        l.add(numToStr.apply(grpSeq));

        //cpToUnicodeName,cpToUnicodeScriptName,cpToUnicodeBlockName
        for(Map.Entry<S,Function<N,S>> singleArgFunctionInNumOutStrMapEntry : singleArgFunctionInNumOutStrMap.entrySet()){
            if(1L==suppressColumnsMap.get(singleArgFunctionInNumOutStrMapEntry.getKey()).stream().filter(e->e.equals(OFF)).count()){

            }else{
                l.add(singleArgFunctionInNumOutStrMapEntry.getValue().apply(i));
            }
        }
        rt.put(seq,l);

        S dest = null;
        for(Map.Entry<S,List<S>> suppressColumnsMapEntry: suppressColumnsMap.entrySet()){
            if(NORMALIZE_PATTERN_MAP.keySet().stream().anyMatch(e->e.equals(suppressColumnsMapEntry.getKey()))){
                if(1L==suppressColumnsMapEntry.getValue().stream().filter(e->e.equals(OFF)).count()){

                }else{
                    dest = strToNorm.apply(cpToStr.apply(i),NORMALIZE_PATTERN_MAP.get(suppressColumnsMapEntry.getKey()));//strToNorm(cpToStr(i), Normalizer.Form.NFD)
                    l.add(dest);
                    rt.put(seq,l);
                    //strToUtf8,strToUtf16,strToUtf32,strToUnicode
                    for(Map.Entry<S,Function<S,S>> singleArgFunctionInStrOutStrMapEntry : singleArgFunctionInStrOutStrMap.entrySet()){
                        //デフォルト値の設定
                        if(rt.containsKey(seq)){
                            //紐づくキーがあれば、リスト追加
                            if(dest==null){
                                //正規化無しの場合
                                if(1L==suppressColumnsMap.get(singleArgFunctionInStrOutStrMapEntry.getKey()).stream().filter(e->e.equals(OFF)).count()){

                                }else{
                                    rt.get(seq).addAll(new ArrayList<>(Arrays.asList(singleArgFunctionInStrOutStrMapEntry.getValue().apply(cpToStr.apply(i)))));
                                }
                            }else{
                                //正規化有りの場合
                                if(1L==suppressColumnsMap.get(singleArgFunctionInStrOutStrMapEntry.getKey()).stream().filter(e->e.equals(OFF)).count()){

                                }else{
                                    rt.get(seq).addAll(new ArrayList<>(Arrays.asList(singleArgFunctionInStrOutStrMapEntry.getValue().apply(dest))));
                                }
                            }
                        }else{
                            //紐づくキーは直前のループで追加済み
                        }
                    }
                }
            }else{

            }
        }
        return rt;
    }

    private static Map<Integer, List<String>> wrapperExecuteMkTbl(Integer s,Integer e,Map<String, List<String>> suppressColumnsMap) {
        Map<Integer, List<String>> rt = new LinkedHashMap<>();
        //結果列作成用の取得列定義マップ

        ++GRP_CNT;
        for(int i=s;i<=e;i++){
            rt.putAll(executeMkTbl(++SEQ_CNT,GRP_CNT,(i-s+1),i,SINGLE_ARGS_FUNCTION_IN_NUM_OUT_STR_MAP,SINGLE_ARGS_FUNCTION_IN_STR_OUT_STR_MAP,MULTIPLE_ARGS_FUNCTION_MAP,suppressColumnsMap));
        }
        return rt;
    }

    private static String repeat(String str, int n) {
        return Stream.generate(() -> str).limit(n).collect(Collectors.joining());
    }
    private static String salt(String str,String prefix,String suffix) {
        return Stream.of(str).collect(Collectors.joining("",prefix,suffix));
    }
    private static List<String> ngram (String s,Integer n){
        List<String> rt = new ArrayList<>();
        String ngramPtn = Stream.of(repeat(".",n)).map(e->salt(e,"(?=(","))")).collect(Collectors.joining());
        Pattern p = Pattern.compile(ngramPtn);
        Matcher m = p.matcher(s);
        while(m.find()){
            rt.add(m.group(1));
        }
        return rt;
    }
    private static void debug(Map<Integer,List<String>> map){
        for(Map.Entry<Integer,List<String>> entry : map.entrySet()){
            System.out.printf("%s\t%s\n",entry.getKey(),entry.getValue().stream().collect(Collectors.joining("\t")));
        }
    }
    private static List<List<String>> grpKeTsuBanMnMx(List<Integer> r){
        Map<Integer,Integer> m = r.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toMap(e->e,e->e+1,(pre,post)->post,LinkedHashMap::new));
        StringBuilder sb = new StringBuilder();
        Integer preKey=-1;
        for(Map.Entry<Integer,Integer> entry : m.entrySet()){
            if(entry.getKey().intValue()==preKey){
                sb.append(","+entry.getKey().intValue());
            }else{
                sb.append("\n"+","+entry.getKey());
            }
            preKey = entry.getValue().intValue();
        }
        return Stream.of(sb.toString()).flatMap(e->Arrays.asList(e.split("\n")).stream().filter(ee->0!=ee.length())).map(ee->Arrays.asList(Arrays.asList(ee.split(",")).stream().filter(eee->0!=eee.length()).toArray(String[]::new))).collect(Collectors.toList());
    }
    private static Map<Integer, List<String>> searchTbl(Integer startRn,Integer endRn,Map<String, List<String>> suppressColumnsMap){
        Map<Integer, List<String>> rt = null;
        rt= wrapperExecuteMkTbl(startRn,endRn,suppressColumnsMap);
        return rt;
    }
    private static Map<Integer,List<Integer>> grpStartEndRn(List<Integer> r){
        List<List<String>> ll = grpKeTsuBanMnMx(r);
        Map<Integer,List<Integer>> rt = new HashMap<>();
        int row=ll.size();
        for(int i=0;i<row;i++){
            rt.put(i,Arrays.asList(Integer.valueOf(ll.get(i).stream().min(Comparator.comparing(e->Integer.valueOf(e))).get())
                    ,Integer.valueOf(ll.get(i).stream().max(Comparator.comparing(e->Integer.valueOf(e))).get())));
        }
        return rt;
    }
    private static Integer printOut(Map<Integer,List<Integer>> rr,Map<String, List<String>> suppressColumnsMap){
        int ret = SUCCESS_STATUS;
        int cnt = 0;
        for(int i=0;i<rr.size();i++){
            cnt+=(rr.get(i).get(1)-rr.get(i).get(0)+1);
            debug(searchTbl(rr.get(i).get(0),rr.get(i).get(1),suppressColumnsMap));
        }
        return ret+cnt;
    }
    private static <K,V,N,S> Set<N> wrapperExecuteSearch(
            K startRn
            ,V endRn
            ,N ngramCnt
            ,Map<S,List<S>> searchArgsMap
            ,List<S> noneKeyWord
            ,Map<S,BiFunction<K,V,Map<N,S>>> mkInputFunctionMap
            ,Map<S,BiFunction<Map<N, S>,N,Map<S, List<S>>>> splitProcessFunctionMap
            ,Map<S,BiFunction<Map<N,S>,S,Set<N>>> filterProcessFunctionMap
            ,Map<S,Function<List<S>,Set<N>>> shapeProcessFunctionMap
    ){
        Set<N> rt;
        List<S> l = searchArgsMap.keySet().stream().collect(Collectors.toList());
        BiFunction<K,V,Map<N,S>> mkInputFunction = mkInputFunctionMap.get(l.get(0));
        BiFunction<Map<N, S>,N,Map<S, List<S>>> splitProcessFunction = splitProcessFunctionMap.get(l.get(1));
        S search_key_word = searchArgsMap.get(l.get(2)).get(0);//複数検索ワードに対応できるようにしておきたいので、こうした

        //IN
        Map<N,S> input = mkInputFunction.apply(startRn,endRn);

        //CMD
        Map<S, List<S>> midput;
        if(WORD_SPLIT_LIST.stream().anyMatch(e->e.equals(l.get(1)))){
            //word
            midput = splitProcessFunction.apply(input,ngramCnt);
        }else if(NGRAM_SPLIT_LIST.stream().anyMatch(e->e.equals(l.get(1)))){
            //ngram
            midput = splitProcessFunction.apply(input,ngramCnt);
        }else{
            //hash
            BiFunction<Map<N,S>,S,Set<N>> filterProcessFunction = filterProcessFunctionMap.get(OPTION_MK_IDX_FILTER);
            rt = filterProcessFunction.apply(input,search_key_word);
            return rt;
        }

        //OUT
        List<S> output = Optional.ofNullable(midput.get(search_key_word)).orElse(noneKeyWord);
        if(output.get(0).equals(noneKeyWord.get(0))){
            return new HashSet<>();
        }else{
            Function<List<S>,Set<N>> shapeProcessFunction = shapeProcessFunctionMap.get(OPTION_MK_IDX_SHAPE);
            rt = shapeProcessFunction.apply(output);
        }
        return rt;
    }
    private static Set<Integer> searchCodePointStartEnd(List<Map<String,List<String>>> searchArgsMapList){
        Set<Integer> rt = new HashSet<>();
        int mx = searchArgsMapList.size();
        for(int i=0;i<mx;i++){
            Integer ngramCnt = searchArgsMapList.get(i).get(OPTION_SEARCH_KEYWORD).get(0).length();
            rt.addAll(wrapperExecuteSearch(
                    DEFAULT_START_RN
                    ,DEFAULT_END_RN
                    ,ngramCnt
                    ,searchArgsMapList.get(i)
                    ,Arrays.asList(DEFAULT_NONE_KEYWORD)
                    ,MK_INPUT_FUNCTION_MAP
                    ,SPLIT_PROCESS_FUNCTION_MAP
                    ,FILTER_PROCESS_FUNCTION_MAP
                    ,SHAPE_PROCESS_FUNCTION_MAP
            ));
        }
        return rt;
    }

    private static Map<String, Pattern> mkOptionArgsRegexpPattern(Map<String, List<String>> optionArgsPattern){
        Map<String, Pattern> rt = new LinkedHashMap<>();
        for(Map.Entry<String,List<String>> entry : optionArgsPattern.entrySet()){
            Pattern ptn = Pattern.compile("((?!.)." + entry.getValue().stream().map("|"::concat).collect(Collectors.joining()) + ")");
            rt.put(entry.getKey(),ptn);
        }
        return rt;
    }

    private static boolean isSearchKeyword (String s,List<String> ptnList) {
        return ptnList.stream().anyMatch(ptn -> s.matches(ptn));
    }

    private static List<Map<String, List<String>>> setOptionArgsPattern (List<String> cmdLineArgs,Map<String, Pattern> optionArgsRegexpPatternMap){
        List<Map<String, List<String>>> rt = new ArrayList<>();

        if(cmdLineArgs.size()<=0){
            //引数個数チェック
            optionUsage(FAILURE_STATUS,OPTION_HELP);
        }

        int mx = cmdLineArgs.size();

        Map<String, List<String>> optionFlgPatternOnMap = new LinkedHashMap<>();
        Map<String, List<String>> optionFlgPatternOffMap = new LinkedHashMap<>();
        for(int i=0;i<mx;i++){
            //オプション引数が与えた際のフラグ設定 ON 与えられた分だけ設定
            for(Map.Entry<String,List<String>> entry : OPTION_FLG_PATTERN.get(ON).entrySet()){
                if(entry.getKey().equals(OPTION_SEARCH_KEYWORD)){
                    //コマンドライン引数が検索キーワードの場合
                    if(isSearchKeyword(cmdLineArgs.get(i),OPTION_FLG_PATTERN.get(ON).get(OPTION_SEARCH_KEYWORD))){
                        optionFlgPatternOnMap.put(OPTION_SEARCH_KEYWORD,Arrays.asList(cmdLineArgs.get(i)));
                    }else{

                    }
                }else{
                    //コマンドライン引数が検索キーワードでない場合
                    if(optionArgsRegexpPatternMap.get(entry.getKey()).matcher(cmdLineArgs.get(i)).matches()){
                        optionFlgPatternOnMap.put(entry.getKey(),entry.getValue());
                    }
                }
            }
            //オプション引数が与えた際のフラグ設定 OFF 与えられた分だけ設定
            for(Map.Entry<String,List<String>> entry : OPTION_FLG_PATTERN.get(OFF).entrySet()){
                if(optionArgsRegexpPatternMap.get(entry.getKey()).matcher(cmdLineArgs.get(i)).matches()){
                    optionFlgPatternOffMap.put(entry.getKey(),entry.getValue());
                }
            }
            rt.addAll(optionFlgPatternOnMap.entrySet().stream().parallel().map(e->Map.of(e.getKey(),e.getValue())).collect(Collectors.toList()));
            rt.addAll(optionFlgPatternOffMap.entrySet().stream().parallel().map(e->Map.of(e.getKey(),e.getValue())).collect(Collectors.toList()));
            optionFlgPatternOnMap = new LinkedHashMap<>();
            optionFlgPatternOffMap = new LinkedHashMap<>();
        }

        //オプション引数が与えられなかった際のデフォルトフラグ設定 ON
        Map<String, List<String>> defaultOptionFlgPatternOnMap = new LinkedHashMap<>();
        for(Map.Entry<String,List<String>> entry : OPTION_DEFAULT_FLG_PATTERN.get(ON).entrySet()){
            if(rt.stream().flatMap(map->map.keySet().stream()).noneMatch(key->key.contains(entry.getKey()))){
                //未登録エントリのみ設定
                defaultOptionFlgPatternOnMap.put(entry.getKey(),entry.getValue());
            }
        }
        //Map<String, List<String>> --> List<Map<String, List<String>>>
        rt.addAll(defaultOptionFlgPatternOnMap.entrySet().stream().parallel().map(e->Map.of(e.getKey(),e.getValue())).collect(Collectors.toList()));

        //オプション引数が与えられなかった際のデフォルトフラグ設定 OFF
        Map<String, List<String>> defaultOptionFlgPatternOffMap = new LinkedHashMap<>();
        for(Map.Entry<String,List<String>> entry : OPTION_DEFAULT_FLG_PATTERN.get(OFF).entrySet()){
            if(rt.stream().flatMap(map->map.keySet().stream()).noneMatch(key->key.contains(entry.getKey()))){
                //未登録エントリのみ設定
                defaultOptionFlgPatternOffMap.put(entry.getKey(),entry.getValue());
            }
        }
        //Map<String, List<String>> --> List<Map<String, List<String>>>
        rt.addAll(defaultOptionFlgPatternOffMap.entrySet().stream().parallel().map(e->Map.of(e.getKey(),e.getValue())).collect(Collectors.toList()));
        return rt;
    }
    private static boolean showCmdInfo(List<Map<String,List<String>>> mainReStyleProcessArgsList,String option){
        return 0!=mainReStyleProcessArgsList.stream().flatMap(m->m.entrySet().stream())
                    .filter(e->Stream.of(option).anyMatch(s->e.getKey().contains(s)))
                    .filter(e->e.getValue().contains(ON)).count();
    }
    private static Integer mainProcess (List<Map<String,List<String>>> mainReStyleProcessArgsList) {
        int ret = SUCCESS_STATUS;
        List<String> argsKeyList = mainReStyleProcessArgsList.stream().flatMap(e->e.keySet().stream()).collect(Collectors.toList());
        Map<String,List<String>> suppressColMap = mainReStyleProcessArgsList.stream().flatMap(m->m.entrySet().stream()).filter(e->COL_ARGS_LIST.contains(e.getKey()))
                .collect(Collectors.toMap(
                        e->e.getKey()
                        ,e->e.getValue()
                        ,(pre,post)->post
                        ,LinkedHashMap::new
                ));
        List<Map<String,List<String>>> searchArgsMapList = mainReStyleProcessArgsList.stream().filter(e-> 3==e.values().size()).collect(Collectors.toList());
        finish : {
            if(showCmdInfo(mainReStyleProcessArgsList,OPTION_HELP)){
                optionUsage(ret,OPTION_HELP);
                break finish ;
            }
            if(showCmdInfo(mainReStyleProcessArgsList,OPTION_USAGE)){
                optionUsage(ret,OPTION_USAGE);
                break finish ;
            }
            if(showCmdInfo(mainReStyleProcessArgsList,OPTION_VERSION)){
                optionUsage(ret,OPTION_VERSION);
                break finish ;
            }
            Set<Integer> rt;
            //ここは実行優先度を管理することになる
            if(grpArgsList.stream().noneMatch(e->argsKeyList.contains(e))){
                //rangeのみ指定
                rt = IntStream.rangeClosed(DEFAULT_START_RN,DEFAULT_END_RN).boxed().parallel().collect(Collectors.toSet());
                ret += printOut(grpStartEndRn(rt.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList())),suppressColMap);
                break finish ;
            }else{
                //range以外指定（検索）
                rt = searchCodePointStartEnd(searchArgsMapList);
                ret += printOut(grpStartEndRn(rt.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList())),suppressColMap);
            }
        }
        return ret;
    }
    private static void canYouHelpMe(List<Map<String, List<String>>> mainProcessArgs){
        for(Map<String, List<String>> map : mainProcessArgs){
            for(Map.Entry<String, List<String>> entry : map.entrySet()){
                if(Stream.of(OPTION_HELP).anyMatch(e->e.contains(entry.getKey())) && entry.getValue().get(0).contains(ON)){
                    optionUsage(SUCCESS_STATUS,OPTION_HELP);
                }
                if(Stream.of(OPTION_USAGE).anyMatch(e->e.contains(entry.getKey())) && entry.getValue().get(0).contains(ON)){
                    optionUsage(SUCCESS_STATUS,OPTION_USAGE);
                }
                if(Stream.of(OPTION_VERSION).anyMatch(e->e.contains(entry.getKey())) && entry.getValue().get(0).contains(ON)){
                    optionUsage(SUCCESS_STATUS,OPTION_VERSION);
                }
            }
        }
    }

    private static void mainProcessArgsGroupingChk(Map<Integer,Map<String,List<String>>> mainGroupingProcessArgs){
        for(Map.Entry<Integer,Map<String,List<String>>> map : mainGroupingProcessArgs.entrySet()){
            if(0L<map.getValue().keySet().stream().filter(e->e.contains("INPUT")||e.contains("SPLIT")||e.contains("HASH")||e.contains("KEYWORD")).count()){
                if(3!=map.getValue().keySet().stream().collect(Collectors.toList()).stream().filter(e->grpArgsList.contains(e)).count()){
                    int ret=SUCCESS_STATUS;
                    optionUsage(ret,OPTION_HELP);
                }else{

                }
            }else{

            }
        }
    }

    private static Map<Integer,Map<String,List<String>>> mainGroupingProcessArgs(List<Map<String, List<String>>> mainProcessArgs){
        int grp=0;
        Map<Integer,Map<String,List<String>>> rt = new LinkedHashMap<>();
        for(Map<String, List<String>> map : mainProcessArgs){
            for(Map.Entry<String, List<String>> entry : map.entrySet()){
                if(entry.getKey().contains("INPUT")||entry.getKey().contains("SPLIT")||entry.getKey().contains("HASH")||entry.getKey().contains("KEYWORD")){
                    //いずれかのグルーピングキーを含んでいる場合
                    if(entry.getKey().contains("INPUT")){
                        //グルーピングの開始点がある場合
                        grp++;//インクリメントしてから処理
                        if(rt.containsKey(grp)){
                            //紐づくキーがあれば、リスト追加
                            rt.get(grp).put(entry.getKey(),entry.getValue());
                        }else{
                            //紐づくキーがなければ、リスト新規追加
                            rt.put(grp,new LinkedHashMap<>(){{put(entry.getKey(),entry.getValue());}});
                        }
                    }else {
                        //グルーピングの開始点がない場合
                        if(rt.containsKey(grp)){
                            //紐づくキーがあれば、リスト追加
                            rt.get(grp).put(entry.getKey(),entry.getValue());
                        }else{
                            //紐づくキーがなければ、リスト新規追加
                            rt.put(grp,new LinkedHashMap<>(){{put(entry.getKey(),entry.getValue());}});
                        }
                    }
                }else{
                    //いずれのグルーピングキーも含んでいない場合
                    grp++;//インクリメントしてから処理

                    if(rt.containsKey(grp)){
                        //紐づくキーがあれば、リスト追加
                        rt.get(grp).put(entry.getKey(),entry.getValue());
                    }else{
                        //紐づくキーがなければ、リスト新規追加
                        rt.put(grp,new LinkedHashMap<>(){{put(entry.getKey(),entry.getValue());}});
                    }
                }
            }
        }
        return rt;
    }

    private static void setDefaultCodePointRange(List<String> cmdLineArgs, Map<String,Pattern> optionArgsRegexpPatternMap){
        //システム全体で参照するコードポイント範囲を設定
        int mx = cmdLineArgs.size();
        for(int i = 0;i < mx;i++){

            Matcher mc = optionArgsRegexpPatternMap.get(OPTION_RANGE).matcher(cmdLineArgs.get(i));

            if(mc.matches()){

                List<String> l = Arrays.asList(cmdLineArgs.get(i).split(ARGS_SEPARATOR));

                if(l.size()>3){
                    //引数個数チェック
                    optionUsage(FAILURE_STATUS,OPTION_HELP);
                }
                if(2==l.size()){
                    if(l.get(1).length()>String.valueOf(DEFAULT_END_RN).length()){
                        //桁数チェック
                        optionUsage(FAILURE_STATUS,OPTION_HELP);
                    }
                    if(Integer.parseInt(l.get(1))>DEFAULT_END_RN){
                        //最大値チェック
                        optionUsage(FAILURE_STATUS,OPTION_HELP);
                    }
                    if(Integer.parseInt(l.get(1))<DEFAULT_END_RN){
                        DEFAULT_END_RN = Integer.parseInt(l.get(1));
                    }
                }else if(3==l.size()){
                    if(l.get(1).length()>String.valueOf(DEFAULT_END_RN).length()){
                        //桁数チェック
                        optionUsage(FAILURE_STATUS,OPTION_HELP);
                    }
                    if(l.get(2).length()>String.valueOf(DEFAULT_END_RN).length()){
                        //桁数チェック
                        optionUsage(FAILURE_STATUS,OPTION_HELP);
                    }
                    if(Integer.parseInt(l.get(2))<DEFAULT_START_RN){
                        //最小値チェック
                        optionUsage(FAILURE_STATUS,OPTION_HELP);
                    }
                    DEFAULT_START_RN = Integer.parseInt(l.get(1));
                    DEFAULT_END_RN = Integer.parseInt(l.get(2));
                }else{
                    optionUsage(FAILURE_STATUS,OPTION_HELP);
                }
            }
        }
    }

    public static void main(String... cmdLineArgs) {
        int ret;

        Map<String,Pattern> optionArgsRegexpPattern = mkOptionArgsRegexpPattern(OPTION_ARGS_PATTERN);


        setDefaultCodePointRange(Arrays.asList(cmdLineArgs),optionArgsRegexpPattern);

        List<Map<String, List<String>>> mainProcessArgs = setOptionArgsPattern(Arrays.asList(cmdLineArgs),optionArgsRegexpPattern);

        canYouHelpMe(mainProcessArgs);

        Map<Integer,Map<String,List<String>>> mainGroupingProcessArgs = mainGroupingProcessArgs(mainProcessArgs);

        mainProcessArgsGroupingChk(mainGroupingProcessArgs);

        int mx = mainGroupingProcessArgs.size();
        List<Map<String,List<String>>> mainReStyleProcessArgs = IntStream.rangeClosed(1,mx).boxed().map(k->mainGroupingProcessArgs.get(k)).collect(Collectors.toList());

        ret = mainProcess(mainReStyleProcessArgs);
        System.exit(ret);
    }
}
# unidat
java製のunicodeデータ生成コマンドラインツール

graalvmでネイティブバイナリで単一実行ファイルを生成できた

# 事前準備

```
cd /usr/local/src && \
curl -LO https://download.java.net/openjdk/jdk11/ri/openjdk-11+28_linux-x64_bin.tar.gz && \
tar xvf openjdk-11+28_linux-x64_bin.tar.gz

export JAVA_HOME=/usr/local/src/jdk-11
export PATH=$JAVA_HOME/bin:$PATH
```

# インストール

rootユーザーで

[Release](https://github.com/ukijumotahaneniarukenia/unidat/releases)

```
$ wget -q https://github.com/ukijumotahaneniarukenia/unidat/releases/download/1-0-0/unidat -O /usr/local/bin/unidat

$ ls -lh /usr/local/bin/unidat
-rwxr-xr-x. 1 root root 6.7M  6月  2 18:14 /usr/local/bin/unidat
```


# ヘルプ

```
$ unidat
unidat --range:1:30
unidat --range:50:80 -nfc -nfd
unidat --range:12354:12390 -nfc -nfd -nfkc
unidat --range:12354:12390 -cp -usc -ubl -u8 -u32 --unicode
unidat -input-unicode-name -word-split HIRAGANA
unidat -input-unicode-name -word-split HIRAGANA -input-unicode-block-name -ngram-hyphen-split HAN -nfc -nfd -nfkc
unidat -input-unicode-name -word-split HIRAGANA -input-unicode-name -word-split KATAKANA -input-unicode-name -word-split HIRAGANA  -cp -usc -ubl -u8 -u32 --unicode
unidat -input-unicode-name --hashkey HIRAGANA  -input-unicode-name --hashkey HIRAGANA
unidat -input-unicode-script-name --hash KATAKANA -input-unicode-name --hashkey HIRAGANA -nfc -nfd -nfkc
unidat -nfc -nfd -nfkc -input-unicode-block-name --hashkey HIRAGANA -input-unicode-script-name --hash KATAKANA
unidat -nfc -nfd -nfkc -input-unicode-script-name --hash KATAKANA -nfc -nfd -nfkc -input-unicode-block-name --hashkey HIRAGANA
unidat -input-unicode-block-name -word-underscore-split MISCELLANEOUS -u32 --nfd -nfc -nfkc -u16 -u32 -unicode
unidat -input-unicode-block-name -word-underscore-split PICTOGRAPHS -u32 --nfd -nfc -nfkc -u16 -u32 -unicode
unidat -input-unicode-name -word-split MOYAI -u32 --nfd -nfc -nfkc -u16 -u32 -unicode
unidat -input-unicode-name -word-split SILHOUETTE -u32 --nfd -nfc -nfkc -u16 -u32
unidat -input-unicode-name -word-split SILHOUETTE -u32 --nfd -nfc -nfkc -u16 -u32  -input-unicode-name -word-split MOYAI -u32 --nfd -nfc -nfkc -u16 -u32 -unicode
```


使い方

```
$ unidat -input-unicode-name -word-split SILHOUETTE -u32 --nfd -nfc -nfkc -u16 -u32  -input-unicode-name -word-split MOYAI -u32 --nfd -nfc -nfkc -u16 -u32 -unicode
1	1	1	128100	👤	BUST IN SILHOUETTE	COMMON	MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS	👤	f09f91a4
2	1	2	128101	👥	BUSTS IN SILHOUETTE	COMMON	MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS	👥	f09f91a5
3	2	1	128483	🗣	SPEAKING HEAD IN SILHOUETTE	COMMON	MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS	🗣	f09f97a3
4	3	1	128510	🗾	SILHOUETTE OF JAPAN	COMMON	MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS	🗾	f09f97be
5	3	2	128511	🗿	MOYAI	COMMON	MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS	🗿	f09f97bf

$ unidat --range:70:80 -nfc -nfd
1	1	1	70	F	LATIN CAPITAL LETTER F	LATIN	BASIC_LATIN	F	46	0046	00000046	U+00046	F	46	0046	00000046	U+00046
2	1	2	71	G	LATIN CAPITAL LETTER G	LATIN	BASIC_LATIN	G	47	0047	00000047	U+00047	G	47	0047	00000047	U+00047
3	1	3	72	H	LATIN CAPITAL LETTER H	LATIN	BASIC_LATIN	H	48	0048	00000048	U+00048	H	48	0048	00000048	U+00048
4	1	4	73	I	LATIN CAPITAL LETTER I	LATIN	BASIC_LATIN	I	49	0049	00000049	U+00049	I	49	0049	00000049	U+00049
5	1	5	74	J	LATIN CAPITAL LETTER J	LATIN	BASIC_LATIN	J	4a	004A	0000004A	U+0004A	J	4a	004A	0000004A	U+0004A
6	1	6	75	K	LATIN CAPITAL LETTER K	LATIN	BASIC_LATIN	K	4b	004B	0000004B	U+0004B	K	4b	004B	0000004B	U+0004B
7	1	7	76	L	LATIN CAPITAL LETTER L	LATIN	BASIC_LATIN	L	4c	004C	0000004C	U+0004C	L	4c	004C	0000004C	U+0004C
8	1	8	77	M	LATIN CAPITAL LETTER M	LATIN	BASIC_LATIN	M	4d	004D	0000004D	U+0004D	M	4d	004D	0000004D	U+0004D
9	1	9	78	N	LATIN CAPITAL LETTER N	LATIN	BASIC_LATIN	N	4e	004E	0000004E	U+0004E	N	4e	004E	0000004E	U+0004E
10	1	10	79	O	LATIN CAPITAL LETTER O	LATIN	BASIC_LATIN	O	4f	004F	0000004F	U+0004F	O	4f	004F	0000004F	U+0004F
11	1	11	80	P	LATIN CAPITAL LETTER P	LATIN	BASIC_LATIN	P	50	0050	00000050	U+00050	P	50	0050	00000050	U+00050

```


# 所感

graalvm便利

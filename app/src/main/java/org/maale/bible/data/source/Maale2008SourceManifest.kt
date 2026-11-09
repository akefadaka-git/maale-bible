package org.maale.bible.data.source

/**
 * Metadata-only, Owner-authorized 2008 Maale edition manifest.
 * No scripture text is bundled here. Keep synchronized with
 * content/manifests/maale-2008-source-manifest.json.
 */
object Maale2008SourceManifest {
    const val EDITION_ID = "MDYTWF / mdyeth"
    const val LANGUAGE_CODE = "mdy"
    const val LICENSE = "CC BY-SA 4.0"
    const val ATTRIBUTION = "The Word for the World International; The Bible Society of Ethiopia"
    val expectation = SourceArchiveExpectation(
        1809863L,
        "4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11",
        listOf(
        ExpectedUsfmFile(1, "GEN", "02-GENmdyeth.usfm", 347847L, "cc9b7703d660bd39dfda8a358f9a3fb27d97f05b04f8851b3cf3e536f9e1f392"),
        ExpectedUsfmFile(2, "EXO", "03-EXOmdyeth.usfm", 287970L, "d395ce46dd48494916f455276ddad2832fcb378c7cc6af2044c35cb1f6eec475"),
        ExpectedUsfmFile(3, "LEV", "04-LEVmdyeth.usfm", 197221L, "4736e4a52bb52dfceb6513a121598646c84d13b10bc3bcd1dc64651521612251"),
        ExpectedUsfmFile(4, "NUM", "05-NUMmdyeth.usfm", 260903L, "dfdee1c6017888c5c81098cce66e22f69ab8d6c28ea4fa59fb13a95cf03e12d2"),
        ExpectedUsfmFile(5, "DEU", "06-DEUmdyeth.usfm", 260204L, "3e9c2afc1de1582fbede42964704e4edee9bfa3b2e0b10cb51aad688378501c8"),
        ExpectedUsfmFile(6, "JOS", "07-JOSmdyeth.usfm", 160147L, "ef9f95a9baa685e218c8dad32f023a8400a53e2c931b838e71110996f960f539"),
        ExpectedUsfmFile(7, "JDG", "08-JDGmdyeth.usfm", 160090L, "5b3fd5a7d1f57b98afaacf3f85dc9132573ed78c1a557815343d9236b3e64f39"),
        ExpectedUsfmFile(8, "RUT", "09-RUTmdyeth.usfm", 23607L, "e79ebb48ce7ebdd8cc294f822f81791e4ff5a4edd6e50bbbd08003a194f1fd01"),
        ExpectedUsfmFile(9, "1SA", "10-1SAmdyeth.usfm", 220652L, "753356d5a0c7dc0754f21dbe3ef5aacde8caddc0de605c6096189409dd58b92c"),
        ExpectedUsfmFile(10, "2SA", "11-2SAmdyeth.usfm", 179463L, "ad3f36ea17348030eb5544d2390d3f72d62f83e8737affba8efb07fd3e8f4c59"),
        ExpectedUsfmFile(11, "1KI", "12-1KImdyeth.usfm", 223811L, "0c749f3fc6c9cc46c60f8fc1646c38f0fa3aa57621028efeb367c73d7d2407c3"),
        ExpectedUsfmFile(12, "2KI", "13-2KImdyeth.usfm", 217433L, "8b0a58da0cf8465a893af0cb49f95e57bd6deac87c651793f5b3a8d4c27ed76c"),
        ExpectedUsfmFile(13, "1CH", "14-1CHmdyeth.usfm", 204378L, "f1f4f010f87b51ca4c1cde87dec0c00cf043a91a2f4a52e3df32b222fcd7e074"),
        ExpectedUsfmFile(14, "2CH", "15-2CHmdyeth.usfm", 244443L, "0fe0af6f964e97fcac280017e5851dc8ab22e1a8a10b4e593054c45b7fe58c3f"),
        ExpectedUsfmFile(15, "EZR", "16-EZRmdyeth.usfm", 70205L, "3acac6b4795bf91684b595c78338485d92fefbc325ab4aaf664c824fc655d7bc"),
        ExpectedUsfmFile(16, "NEH", "17-NEHmdyeth.usfm", 104470L, "19891a9886f0527a944da0aa30e1a92db4f8f3118ef72ba65294269926d22ace"),
        ExpectedUsfmFile(17, "EST", "18-ESTmdyeth.usfm", 55673L, "8c7e2e10a84c80c7361eca246eb5769cfe368449103130bb109c61297e477b90"),
        ExpectedUsfmFile(18, "JOB", "19-JOBmdyeth.usfm", 177123L, "bbf22fdee42ef324bd08091dd2cd1010cdf22a88440aa2bb85b597bc6b4d5f91"),
        ExpectedUsfmFile(19, "PSA", "20-PSAmdyeth.usfm", 444677L, "12cedb54022e7339513ee0aecd3536e779d39982c7ab314398604f1106d0a0bc"),
        ExpectedUsfmFile(20, "PRO", "21-PROmdyeth.usfm", 145312L, "edff444c94fdd2e90a39a22b3c8b0fe5ffdd3d8d346fde24664c1fe143751787"),
        ExpectedUsfmFile(21, "ECC", "22-ECCmdyeth.usfm", 49215L, "7ae25f832628039112145202bb75bbd09a9334641f6a132bf8791cedfdb72c8a"),
        ExpectedUsfmFile(22, "SNG", "23-SNGmdyeth.usfm", 30005L, "1673407a116f7814b0d65d448c644f3822d3432149143db3cdfefeda6cf1d3e9"),
        ExpectedUsfmFile(23, "ISA", "24-ISAmdyeth.usfm", 342247L, "d536f5e38442487f3fb24d05b5d4574e65c32668bd6a0d9f7a829bd91213586b"),
        ExpectedUsfmFile(24, "JER", "25-JERmdyeth.usfm", 399179L, "15eb68746f3051119fe277ad6df0eb92906b51e33376b9d3936c6b2ac78f84f4"),
        ExpectedUsfmFile(25, "LAM", "26-LAMmdyeth.usfm", 35137L, "5f31f816c79c4dcbfb84afc8dd080516310077c1401e203eca162135adddd419"),
        ExpectedUsfmFile(26, "EZK", "27-EZKmdyeth.usfm", 350192L, "77a49e9327ef31e67788b5c8ac5485b6f3a3606c6aac0cf90721b25f11b72f6c"),
        ExpectedUsfmFile(27, "DAN", "28-DANmdyeth.usfm", 108477L, "050b88157266e57c2975e45db4dfc5ca56360840aab9bbf44caf81292c735072"),
        ExpectedUsfmFile(28, "HOS", "29-HOSmdyeth.usfm", 59588L, "3f58eae651b38eb18a6e2175e8ccdf28df2f8fa229dd72173dfe79e2000116fc"),
        ExpectedUsfmFile(29, "JOL", "30-JOLmdyeth.usfm", 22282L, "5fa951810a5cf9e5e44c44cd72bea837fbf8c0e94c75153a70b0c44ffe9fb425"),
        ExpectedUsfmFile(30, "AMO", "31-AMOmdyeth.usfm", 45474L, "5b15eeec99046c3161ffa2cc535145bf2a4bb986e8b278acd5e1d34a6e70c837"),
        ExpectedUsfmFile(31, "OBA", "32-OBAmdyeth.usfm", 7793L, "f33aca6387cc0ae5a168bd3df6b518d396e46170b2f66f06cbeae11869b55b98"),
        ExpectedUsfmFile(32, "JON", "33-JONmdyeth.usfm", 13979L, "cfbcf901d81a16991af4fffdb5cb750e184ec7040b3169d1f0b193d56f8bf023"),
        ExpectedUsfmFile(33, "MIC", "34-MICmdyeth.usfm", 32531L, "41a8dc02d51e0278080b6619bd88fd863e91e4ec4c33542437d2214def615900"),
        ExpectedUsfmFile(34, "NAM", "35-NAMmdyeth.usfm", 14238L, "038ff5039b8d7658ebd9dba23e8477694f50d9f46900f2585c2ed79dc33b9760"),
        ExpectedUsfmFile(35, "HAB", "36-HABmdyeth.usfm", 15963L, "47ba742ef44df4668df9a3f90e84ecbe0e4342bd94aea87e6cb3bb72ae57202a"),
        ExpectedUsfmFile(36, "ZEP", "37-ZEPmdyeth.usfm", 18264L, "ba88d85ec98ab30ad7c9f461ea96600fa7d79d2b613d83cf4f27572d36148737"),
        ExpectedUsfmFile(37, "HAG", "38-HAGmdyeth.usfm", 11785L, "de9bfcb5a1771367ec66450d8c1116c9d2c2cfbfceb7950cc6d32ca69a26002c"),
        ExpectedUsfmFile(38, "ZEC", "39-ZECmdyeth.usfm", 62405L, "72aa811169fd38ca44bde16813affb09dbf3a34825647b6352a7a5d989c8c35b"),
        ExpectedUsfmFile(39, "MAL", "40-MALmdyeth.usfm", 19182L, "621b6166cd05e9ef59db1067e73f2a0471b976f43b6413f42ce1bdb08f8d25b1"),
        ExpectedUsfmFile(40, "MAT", "46-MATmdyeth.usfm", 238843L, "d0b9fdeb1d0fe5d13f0a7bd30227c770a7263019d4ca0c8f77896ad5f7b7605c"),
        ExpectedUsfmFile(41, "MRK", "47-MRKmdyeth.usfm", 155376L, "7c884af699bd845b365799b01fa8e05712369da571b65cef7b6bada6f46661a6"),
        ExpectedUsfmFile(42, "LUK", "48-LUKmdyeth.usfm", 258885L, "479d7acb2c756b31a5e5c9f45d83eea3eeef436751c540ef0c79079860b3f1a8"),
        ExpectedUsfmFile(43, "JHN", "49-JHNmdyeth.usfm", 185424L, "68594ef988603b8ad048e183655c30e109edb08c2a588dd27545d48cdef3a07a"),
        ExpectedUsfmFile(44, "ACT", "74-ACTmdyeth.usfm", 243950L, "ea4bb1c08756c49e4c9aa42459ff5fb9a7a0a855f1f8db8a06c6eceeecda46b9"),
        ExpectedUsfmFile(45, "ROM", "51-ROMmdyeth.usfm", 104607L, "3e18c25d221c07693884bb5efebc2d4e2d97220c5e19e34f94518467f7038e95"),
        ExpectedUsfmFile(46, "1CO", "52-1COmdyeth.usfm", 101405L, "52f4fcc1b368d1aeac3f677e1730aefbcf682f4fb3fb8ddc46f6e575defa30e6"),
        ExpectedUsfmFile(47, "2CO", "53-2COmdyeth.usfm", 64340L, "a208e8766a2b0e3fcc05aff9a22b7351440e9529758c75f466623bf4238e713f"),
        ExpectedUsfmFile(48, "GAL", "54-GALmdyeth.usfm", 36299L, "8ed0b931d67c9c79b7bbe94306db707092f09203f7f51e90eea3cfad35ed5658"),
        ExpectedUsfmFile(49, "EPH", "55-EPHmdyeth.usfm", 33743L, "950b26d5f35c94c34e518a12531b8b7689c970353aed92e27e6eddb534eb14be"),
        ExpectedUsfmFile(50, "PHP", "56-PHPmdyeth.usfm", 27303L, "fec6defc979cb840dc4fd101cbcc5f6cfb81869470fa1dffa878bf6773151e73"),
        ExpectedUsfmFile(51, "COL", "57-COLmdyeth.usfm", 25164L, "a8413a32286767aa3aba25fbd8f8a9670bae00086c16f77bb831f596b3ab2754"),
        ExpectedUsfmFile(52, "1TH", "58-1THmdyeth.usfm", 21964L, "663c0f3da45615c2fe33d689c487a11478e815fdf08491a87950007049eb7c3e"),
        ExpectedUsfmFile(53, "2TH", "59-2THmdyeth.usfm", 12443L, "219e40e5c249ab307621e5f30af1628fefc02d6925deaf3dd904e4e7dde07d80"),
        ExpectedUsfmFile(54, "1TI", "60-1TImdyeth.usfm", 27208L, "46c23c343c61906e7f48a0fdac491eccfc97822f42a538a0c6c39823ac6e6e2a"),
        ExpectedUsfmFile(55, "2TI", "61-2TImdyeth.usfm", 20256L, "2fbcf6091cd16eca20de96415d39a95ed5bf40e540a1f18fdcd4b9619c328f78"),
        ExpectedUsfmFile(56, "TIT", "62-TITmdyeth.usfm", 13249L, "29e6ec5aa0ff847395b728963e2096d8e85ec61f0c4de175f45187b31624aa36"),
        ExpectedUsfmFile(57, "PHM", "63-PHMmdyeth.usfm", 6251L, "1040af795a3c1952e9d57c54eb2dcb4a0724a5d224a479ef5cc6258e9410a363"),
        ExpectedUsfmFile(58, "HEB", "64-HEBmdyeth.usfm", 77559L, "68411c70d0f48157fb66954072c0df60ed681b23313ce1e6d94efae6aea86012"),
        ExpectedUsfmFile(59, "JAS", "65-JASmdyeth.usfm", 26183L, "6b341d636765322af4f5b34bb005f2d60d2368ef3f114ccb84a79fa2464f4be1"),
        ExpectedUsfmFile(60, "1PE", "90-1PEmdyeth.usfm", 29603L, "02d4d025a2676c41daf71c2e17df4ac220ad71f8f02a5c533543543068bc5251"),
        ExpectedUsfmFile(61, "2PE", "67-2PEmdyeth.usfm", 17982L, "c71bae4fe198f387796dc65f5ae43b3941d8df98483499d837641c1821baa355"),
        ExpectedUsfmFile(62, "1JN", "68-1JNmdyeth.usfm", 23466L, "f0f1cba07afe5139258e031a2048b3ce04b55bd4c7dffd3ec98d5b58f146bdbf"),
        ExpectedUsfmFile(63, "2JN", "69-2JNmdyeth.usfm", 3683L, "ec16a0869a96dc6aa7a00e1cc0dd74007184d9230a976042feb339643398ab4f"),
        ExpectedUsfmFile(64, "3JN", "94-3JNmdyeth.usfm", 4391L, "687568fcfc7a32a06945b038848bf8cc39353c121f8ea6b3b127c29816ac8bc6"),
        ExpectedUsfmFile(65, "JUD", "71-JUDmdyeth.usfm", 8126L, "bb13cf1ded754618ff8b292cd13d9ddb6e1bf07a3aae03a893225cef89a61d3e"),
        ExpectedUsfmFile(66, "REV", "72-REVmdyeth.usfm", 119864L, "0e37226cb1d8ec94806d2a5b9326c7fcede02907fa9fd8e750ddbe1a41a4be77")
        )
    )
}

package com.everypaisa.parser

class BankParserFactory {
    
    private val parsers: List<BankParser> = listOf(
        HDFCBankParser(),
        ICICIBankParser(),
        SBIParser(),
        AxisBankParser(),
        KotakBankParser(),
        IDFCFirstBankParser(),
        FederalBankParser(),
        PNBParser(),
        BOBParser(),
        CanaraParser(),
        UnionBankParser(),
        EmiratesNBDParser(), // UAE bank
        GooglePayParser(),
        PhonePeParser(),
        PaytmParser(),
        AmazonPayParser(),
        GenericBankParser() // Fallback parser for any bank
    )
    
    fun parse(sender: String, message: String): ParsedTransaction? {
        val parser = parsers.firstOrNull { it.canParse(sender, message) }
        return parser?.parse(sender, message)
    }
    
    fun getMatchingParser(sender: String, message: String): BankParser? {
        return parsers.firstOrNull { it.canParse(sender, message) }
    }
}

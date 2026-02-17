package com.everypaisa.parser

interface BankParser {
    fun canParse(sender: String, message: String): Boolean
    fun parse(sender: String, message: String): ParsedTransaction?
}

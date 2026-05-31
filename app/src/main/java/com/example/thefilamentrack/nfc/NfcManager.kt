package com.example.thefilamentrack.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable

object NfcManager {

    /** Get the unique hardware UID from any NFC tag */
    fun getTagId(tag: Tag): String {
        return tag.id.joinToString("") { "%02x".format(it) }
    }

    /** Read spool ID written to tag as NDEF text. Returns null if no NDEF data. */
    fun readSpoolId(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            val message = ndef.ndefMessage ?: return null
            val record = message.records.firstOrNull() ?: return null
            val payload = record.payload
            val langLen = payload[0].toInt() and 0x3F
            String(payload, langLen + 1, payload.size - langLen - 1, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        } finally {
            try { ndef.close() } catch (_: Exception) {}
        }
    }

    /**
     * Write spool ID to tag as NDEF text record.
     * Handles both already-formatted and blank unformatted tags.
     * Returns true on success, false on failure.
     */
    fun writeSpoolId(tag: Tag, spoolId: String): Boolean {
        val record = NdefRecord.createTextRecord("en", spoolId)
        val message = NdefMessage(arrayOf(record))

        // Try writing to already-formatted NDEF tag first
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            return try {
                ndef.connect()
                if (!ndef.isWritable) return false
                if (ndef.maxSize < message.toByteArray().size) return false
                ndef.writeNdefMessage(message)
                true
            } catch (e: Exception) {
                false
            } finally {
                try { ndef.close() } catch (_: Exception) {}
            }
        }

        // Tag isn't NDEF formatted — try to format it first
        val formatable = NdefFormatable.get(tag)
        if (formatable != null) {
            return try {
                formatable.connect()
                formatable.format(message)
                true
            } catch (e: Exception) {
                false
            } finally {
                try { formatable.close() } catch (_: Exception) {}
            }
        }

        return false
    }
}
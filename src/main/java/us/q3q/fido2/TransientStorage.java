package us.q3q.fido2;

import javacard.framework.JCSystem;

/**
 * Provides in-memory state in a maximally compact way
 */
public class TransientStorage {
    /**
     * Set of short variables held in memory for generally avoiding flash use
     */
    private final short[] tempShorts;
    /**
     * Used for storing found indices in searches
     */
    private static final short IDX_TEMP_BUF_IDX_STORAGE = 0;
    /**
     * Used for storing found lengths in searches
     */
    private static final short IDX_TEMP_BUF_IDX_LEN = 1;
    /**
     * When writing an overlong response using chained APDUs, stores the position we're up to in the outgoing buffer
     */
    private static final short IDX_CONTINUATION_OUTGOING_WRITE_OFFSET = 2;
    /**
     * When writing an overlong response using chained APDUs, stores the remaining bytes in the outgoing buffer
     */
    private static final short IDX_CONTINUATION_OUTGOING_REMAINING = 3;
    /**
     * When reading an overlong incoming request using chained APDUs, stores the fill level of the incoming buffer
     */
    private static final short IDX_CHAINING_INCOMING_READ_OFFSET = 4;
    /**
     * When reading incoming request chains, which FIDO2 command the request represents (pulled from the first packet)
     */
    private static final short IDX_STORED_COMMAND_BYTE = 5;
    /**
     * How full the scratch buffer is
     */
    private static final short IDX_SCRATCH_ALLOC_SIZE = 6;
    /**
     * Number of times a PIN has been attempted since last reset
     */
    private static final short IDX_PIN_TRIES_SINCE_RESET = 7;
    /**
     * Index of next credential to consider when iterating through RPs with credManagement commands
     */
    private static final short IDX_RP_ITERATION_POINTER = 8;
    /**
     * Index of next credential to consider when iterating through creds with credManagement commands
     */
    private static final short IDX_CRED_ITERATION_POINTER = 9;
    /**
     * Index of next credential (in either allowList or resident keys) to consider when iterating through
     * assertions with getNextAssertion commands
     */
    private static final short IDX_ASSERT_ITERATION_POINTER = 10;
    /**
     * Total number of in-memory short variables
     */
    private static final short NUM_TEMP_SHORTS = 11;

    /**
     * array of per-reset booleans used internally
     */
    private final boolean[] tempBools;
    /**
     * set when authenticator key initialized
     */
    private static final short BOOL_IDX_RESET_PLATFORM_KEY_SET = 0;
    /**
     * set when PIN has been provided (device unlocked)
     */
    private static final short BOOL_IDX_RESET_PIN_PROVIDED = 1;
    /**
     * set if platform supports authenticator-compatible key
     */
    private static final short BOOL_IDX_RESET_FOUND_KEY_MATCH = 2;
    /**
     * set if the "up" (User Presence) option is enabled
     */
    private static final short BOOL_IDX_OPTION_UP = 3;
    /**
     * set if the "uv" (User Validation) option is enabled
     */
    private static final short BOOL_IDX_OPTION_UV = 4;
    /**
     * set if the "rk" (Resident Key) option is enabled
     */
    private static final short BOOL_IDX_OPTION_RK = 5;
    /**
     * set if chaining responses should come from the getNextAssertion buffer
     */
    private static final short BOOL_IDX_RESPONSE_FROM_SCRATCH = 6;
    /**
     * For reset "protection" feature, checks if a reset request has been received since the last authenticator powerup
     */
    private static final short BOOL_IDX_RESET_RECEIVED_SINCE_POWERON = 7;
    /**
     * number of booleans total in array
     */
    private static final short NUM_RESET_BOOLS = 8;

    public TransientStorage() {
        tempShorts = JCSystem.makeTransientShortArray(NUM_TEMP_SHORTS, JCSystem.CLEAR_ON_DESELECT);
        tempBools = JCSystem.makeTransientBooleanArray(NUM_RESET_BOOLS, JCSystem.CLEAR_ON_DESELECT);
    }

    public void fullyReset() {
        for (short s = 0; s < tempShorts.length; s++) {
            tempShorts[s] = 0;
        }
        for (short s = 0; s < tempBools.length; s++) {
            tempBools[s] = false;
        }

    }

    public void clearIterationPointers() {
        tempShorts[IDX_CHAINING_INCOMING_READ_OFFSET] = 0;
        tempShorts[IDX_CRED_ITERATION_POINTER] = 0;
        tempShorts[IDX_RP_ITERATION_POINTER] = 0;
    }

    public short getScratchFillLevel() {
        return tempShorts[IDX_SCRATCH_ALLOC_SIZE];
    }

    public void increaseScratchFillLevel(short numBytes) {
        tempShorts[IDX_SCRATCH_ALLOC_SIZE] += numBytes;
    }

    public void decreaseScratchFillLevel(short numBytes) {
        tempShorts[IDX_SCRATCH_ALLOC_SIZE] -= numBytes;
    }

    public void clearScratchFillLevel() {
        tempShorts[IDX_SCRATCH_ALLOC_SIZE] = 0;
    }

    public void clearAssertIterationPointer() {
        tempShorts[IDX_ASSERT_ITERATION_POINTER] = 0;
    }

    public short getChainIncomingReadOffset() {
        return tempShorts[IDX_CHAINING_INCOMING_READ_OFFSET];
    }

    public void resetChainIncomingReadOffset() {
        tempShorts[IDX_CHAINING_INCOMING_READ_OFFSET] = 0;
    }

    public void increaseChainIncomingReadOffset(short amt) {
        tempShorts[IDX_CHAINING_INCOMING_READ_OFFSET] += amt;
    }

    public short getPinTriesSinceReset() {
        return tempShorts[IDX_PIN_TRIES_SINCE_RESET];
    }

    public void clearPinTriesSinceReset() {
        tempShorts[IDX_PIN_TRIES_SINCE_RESET] = 0;
    }

    public void incrementPinTriesSinceReset() {
        tempShorts[IDX_PIN_TRIES_SINCE_RESET]++;
    }

    public void setResetPinProvided() {
        tempBools[BOOL_IDX_RESET_PIN_PROVIDED] = true;
    }

    public boolean isResetPinProvided() {
        return tempBools[BOOL_IDX_RESET_PIN_PROVIDED];
    }

    public void setPlatformKeySet() {
        tempBools[BOOL_IDX_RESET_PLATFORM_KEY_SET] = true;
    }

    public boolean isPlatformKeySet() {
        return tempBools[BOOL_IDX_RESET_PLATFORM_KEY_SET];
    }

    public void clearOnDeselect() {
        clearIterationPointers();

        tempBools[BOOL_IDX_RESET_PLATFORM_KEY_SET] = false;
        tempShorts[IDX_CONTINUATION_OUTGOING_WRITE_OFFSET] = 0;
        tempBools[BOOL_IDX_RESET_PIN_PROVIDED] = false;
    }

    public void readyStoredVars() {
        tempShorts[IDX_TEMP_BUF_IDX_STORAGE] = 0;
        tempShorts[IDX_TEMP_BUF_IDX_LEN] = -1;
    }

    public void setStoredVars(short idx, short len) {
        tempShorts[IDX_TEMP_BUF_IDX_STORAGE] = idx;
        tempShorts[IDX_TEMP_BUF_IDX_LEN] = len;
    }

    public short getStoredIdx() {
        return tempShorts[IDX_TEMP_BUF_IDX_STORAGE];
    }

    public short getStoredLen() {
        return tempShorts[IDX_TEMP_BUF_IDX_LEN];
    }

    public void resetFoundKeyMatch() {
        tempBools[BOOL_IDX_RESET_FOUND_KEY_MATCH] = false;
    }

    public boolean hasFoundKeyMatch() {
        return tempBools[BOOL_IDX_RESET_FOUND_KEY_MATCH];
    }

    public void setFoundKeyMatch() {
        tempBools[BOOL_IDX_RESET_FOUND_KEY_MATCH] = true;
    }

    public void defaultOptions() {
        tempBools[BOOL_IDX_OPTION_UP] = true;
        tempBools[BOOL_IDX_OPTION_UV] = false;
        tempBools[BOOL_IDX_OPTION_RK] = false;
    }

    public boolean hasRKOption() {
        return tempBools[BOOL_IDX_OPTION_RK];
    }

    public void setRKOption(boolean val) {
        tempBools[BOOL_IDX_OPTION_RK] = val;
    }

    public boolean hasUPOption() {
        return tempBools[BOOL_IDX_OPTION_UP];
    }

    public void setUPOption(boolean val) {
        tempBools[BOOL_IDX_OPTION_UP] = val;
    }

    public boolean hasUVOption() {
        return tempBools[BOOL_IDX_OPTION_UV];
    }

    public void setUVOption(boolean val) {
        tempBools[BOOL_IDX_OPTION_UV] = val;
    }

    public short getAssertIterationPointer() {
        return tempShorts[IDX_ASSERT_ITERATION_POINTER];
    }

    public void setAssertIterationPointer(short val) {
        tempShorts[IDX_ASSERT_ITERATION_POINTER] = val;
    }

    public void setResponseFromScratch() {
        tempBools[BOOL_IDX_RESPONSE_FROM_SCRATCH] = true;
    }

    public boolean isResponseFromScratch() {
        return tempBools[BOOL_IDX_RESPONSE_FROM_SCRATCH];
    }

    public void clearResponseFromScratch() {
        tempBools[BOOL_IDX_RESPONSE_FROM_SCRATCH] = false;
    }

    public void setOutgoingContinuation(short offset, short remaining) {
        tempShorts[IDX_CONTINUATION_OUTGOING_WRITE_OFFSET] = offset;
        tempShorts[IDX_CONTINUATION_OUTGOING_REMAINING] = remaining;
    }

    public void clearOutgoingContinuation() {
        tempShorts[IDX_CONTINUATION_OUTGOING_WRITE_OFFSET] = 0;
        tempShorts[IDX_CONTINUATION_OUTGOING_REMAINING] = 0;
    }

    public short getOutgoingContinuationOffset() {
        return tempShorts[IDX_CONTINUATION_OUTGOING_WRITE_OFFSET];
    }

    public short getOutgoingContinuationRemaining() {
        return tempShorts[IDX_CONTINUATION_OUTGOING_REMAINING];
    }

    public byte getStoredCommandByte() {
        return (byte) tempShorts[IDX_STORED_COMMAND_BYTE];
    }

    public void setStoredCommandByteIfNone(byte v) {
        if (tempShorts[IDX_STORED_COMMAND_BYTE] == 0x00) {
            tempShorts[IDX_STORED_COMMAND_BYTE] = (short)(0xFF & v);
        }
    }

    public void clearStoredCommandByte() {
        tempShorts[IDX_STORED_COMMAND_BYTE] = 0x00;
    }

    public boolean isResetCommandSentSincePoweron() {
        return tempBools[BOOL_IDX_RESET_RECEIVED_SINCE_POWERON];
    }

    public void setResetCommandSentSincePoweron() {
        tempBools[BOOL_IDX_RESET_RECEIVED_SINCE_POWERON] = true;
    }

    public short getRPIterationPointer() {
        return tempShorts[IDX_RP_ITERATION_POINTER];
    }

    public void setRPIterationPointer(short val) {
        tempShorts[IDX_RP_ITERATION_POINTER] = val;
    }

    public short getCredIterationPointer() {
        return tempShorts[IDX_CRED_ITERATION_POINTER];
    }

    public void setCredIterationPointer(short val) {
        tempShorts[IDX_CRED_ITERATION_POINTER] = val;
    }
}
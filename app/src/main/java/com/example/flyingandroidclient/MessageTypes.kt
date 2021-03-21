package com.example.flyingandroidclient

enum class MessageTypes {
    CONTROLS,
    INFO
}

enum class Controls {
    SET_PITCH_AND_ROLL,
    SET_PITCH_PROP_COEF,
    SET_PITCH_INT_COEF,
    SET_PITCH_DER_COEF,
    SET_ROLL_PROP_COEF,
    SET_ROLL_INT_COEF,
    SET_ROLL_DER_COEF,
    SET_ACC_TRUST,
    SET_INCL_CH_RATE_FILTERING_COEF
};
package com.example.flyingandroidclient

enum class MessageTypes {
    CONTROLS,
    ERRORS_INFO,
    CURRENT_OPTIONS_INFO
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
    SET_INCL_CH_RATE_FILTERING_COEF,
    SET_INCL_FILTERING_COEF,

    SET_HEIGHT_PROP_COEF,
    SET_HEIGHT_INT_COEF,
    SET_HEIGHT_DER_COEF,

    START_SENDING_INFO,
    STOP_SENDING_INFO,

    SET_ACCELERATION,
    SET_DIRECTION,
    SET_BASE_ACCELERATION,

    RESET_TURN_OFF_TRIGGER,
    SET_TURN_OFF_INCLINE_ANGLE,

    SET_YAW_SP_PROP_COEF,
    SET_YAW_SP_INT_COEF,
    SET_YAW_SP_DER_COEF,
    SET_YAW_SP_FILTERING_COEF,

    SET_IMU_LPF_MODE
};
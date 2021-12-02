package com.example.flyingandroidclient

enum class MessageTypes {
    CONTROLS,
    ERRORS_INFO,
    CURRENT_OPTIONS_INFO
}

enum class Controls {
    UNSET,
    SET_PITCH_AND_ROLL,
    SET_DESIRED_HEIGHT_US,
    SET_DESIRED_HEIGHT_BAR,
    SET_ACCELERATION,
    SET_DIRECTION,

    SET_PITCH_PROP_COEF,
    SET_PITCH_INT_COEF,
    SET_PITCH_DER_COEF,

    SET_ROLL_PROP_COEF,
    SET_ROLL_INT_COEF,
    SET_ROLL_DER_COEF,

    SET_YAW_PROP_COEF,
    SET_YAW_INT_COEF,
    SET_YAW_DER_COEF,

    SET_ACC_TRUST,
    SET_MAG_TRUST,

    SET_HEIGHT_PROP_COEF,
    SET_HEIGHT_INT_COEF,
    SET_HEIGHT_DER_COEF,

    START_SENDING_INFO,
    STOP_SENDING_INFO,

    SET_BASE_ACCELERATION,

    RESET_TURN_OFF_TRIGGER,
    SET_TURN_OFF_INCLINE_ANGLE,

    SET_IMU_LPF_MODE,
    CALIBRATE_ESC,
    CALIBRATE_GYRO,
    CALIBRATE_ACC,
    CALIBRATE_MAG,

    SET_PITCH_ADJUST,
    SET_ROLL_ADJUST
};
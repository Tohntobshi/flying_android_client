package com.example.flyingandroidclient

enum class MessageTypes {
    CONTROLS,
    PRIMARY_INFO,
    SECONDARY_INFO
}

enum class Controls {
    UNSET,
    MOVE,
    SET_HEIGHT,
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
    SET_ACC_FILTERING,

    SET_HEIGHT_PROP_COEF,
    SET_HEIGHT_INT_COEF,
    SET_HEIGHT_DER_COEF,

    START_SENDING_SECONDARY_INFO,
    STOP_SENDING_SECONDARY_INFO,

    SET_BASE_ACCELERATION,

    RESET_TURN_OFF_TRIGGER,
    SET_TURN_OFF_INCLINE_ANGLE,

    SET_ACC_LPF_MODE,
    SET_GYRO_LPF_MODE,
    CALIBRATE_ESC,
    CALIBRATE_GYRO,
    CALIBRATE_ACC,
    CALIBRATE_MAG,

    SET_PITCH_ADJUST,
    SET_ROLL_ADJUST,

    START_SENDING_PRIMARY_INFO,
    STOP_SENDING_PRIMARY_INFO,
    RESET_LANDING_FLAG,
    SWITCH_TO_RELATIVE_ACCELERATION,
    SET_RELATIVE_ACCELERATION,

    SET_US_HEIGHT_FILTERING,
    SET_US_HEIGHT_DER_FILTERING,

    SET_PITCH_I_LIMIT,
    SET_ROLL_I_LIMIT,
    SET_YAW_I_LIMIT,
    SET_HEIGHT_I_LIMIT,

    SET_MOTOR_CURVE_A,
    SET_MOTOR_CURVE_B,
    SET_VOLTAGE_DROP_CURVE_A,
    SET_VOLTAGE_DROP_CURVE_B,
    SET_POWER_LOSS_CURVE_A,
    SET_POWER_LOSS_CURVE_B,

    SET_HEIGHT_NEGATIVE_INT_COEF,

    SET_POSITION_PROP_COEF,
    SET_POSITION_DER_COEF,
    SET_POSITION_INT_COEF,
    SET_POSITION_I_LIMIT,
    SET_BAR_HEIGHT_PROP_COEF,
    SET_BAR_HEIGHT_DER_COEF,
    SET_BAR_HEIGHT_INT_COEF,
    SET_BAR_HEIGHT_FILTERING,
    SET_BAR_HEIGHT_DER_FILTERING,
    SET_POSITION_FILTERING,
    SET_POSITION_DER_FILTERING,
    SET_HOLD_MODE,
    TAKE_POSITION_CAMERA_SHOT
};
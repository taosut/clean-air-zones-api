DELETE FROM CAZ_PAYMENT_AUDIT.T_CLEAN_AIR_ZONE_PAYMENT_DETAIL;
DELETE FROM CAZ_PAYMENT_AUDIT.T_CLEAN_AIR_ZONE_PAYMENT_MASTER;
DELETE FROM caz_payment.t_clean_air_zone_entrant_payment_match;
DELETE FROM caz_payment.t_clean_air_zone_entrant_payment cascade;
DELETE FROM caz_payment.t_payment;
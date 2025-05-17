import { OrderItemStatusKeys } from "@/utils/constants.ts";
import { Stepper } from "@mantine/core";
import { IconCash, IconHome, IconTruckDelivery } from "@tabler/icons-react";

// Map order item status to step for stepper component
const ORDER_ITEM_STATUS_STEP = {
    PENDING_PAYMENT: 0,
    PENDING_SHIPMENT: 1,
    SHIPPED: 2,
    DELIVERED: 3
} as const;

export type OrderItemStatusStepperProps = {
    status: OrderItemStatusKeys;
}

export function OrderItemStatusStepper({ status }: OrderItemStatusStepperProps) {

    return (
        <Stepper active={ ORDER_ITEM_STATUS_STEP[status] } size="xs" mt="xl">
            <Stepper.Step
                label="Paid"
                icon={ <IconCash/> }
                completedIcon={ <IconCash/> }
                color={
                    (status === "PENDING_SHIPMENT" || status === "SHIPPED"
                        || status === "DELIVERED"
                    ) ? "teal" : ""
                }
            />

            <Stepper.Step
                label="Shipped"
                icon={ <IconTruckDelivery/> }
                completedIcon={ <IconTruckDelivery/> }
                color={
                    (status === "SHIPPED" || status === "DELIVERED")
                        ? "teal" : ""
                }
            />

            <Stepper.Step
                label="Delivered"
                icon={ <IconHome/> }
                completedIcon={ <IconHome/> }
                color={ status === "DELIVERED" ? "teal" : "" }
            />
        </Stepper>
    );
}
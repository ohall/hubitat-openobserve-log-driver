# hubitat-openobserve-log-driver

This Hubitat driver exports log messages to the OpenObserve logging service.

## Create a OpenObserve account and create a log stream
https://cloud.openobserve.ai/

## Install the Driver in Hubitat

1. In the Hubitat web interface, go to the "Drivers Code" section under "Apps Code".
2. Click on the "New Driver" button.
3. Copy and paste the contents of the `OpenObserve.groovy` file into the code editor.
4. Click the "Save" button.
5. Go to the "Devices" section and click on the "Add Device" button.
6. In "Add Device Manually" section click on the "Virtual" button.
7. Select the "OpenObserve" driver from the "Device Type" list, name it, select a room and clink "Create".
8. Configure the device in the "Devices" page, including your OpenObserve [organization](https://openobserve.ai/docs/user-guide/organizations/), [stream](https://openobserve.ai/docs/user-guide/streams/), hostname, and authorization token.
8. Click "Initialize" to start forwarding logs.

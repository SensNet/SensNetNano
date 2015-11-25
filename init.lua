pin = 1
gpio.mode(pin,gpio.INPUT)
if gpio.read(pin) == 1 then
	error("Start interrupted.")
end
remaining, used, total=file.fsinfo()
print("\nFile system info:\nTotal : "..total.." Bytes\nUsed : "..used.." Bytes\nRemain: "..remaining.." Bytes\n")

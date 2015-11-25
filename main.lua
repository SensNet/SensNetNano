function connectedMain(t)
	--file.open("head.html", "r")
	--local head = file.read()
	--file.close()
	print("yeah, we are in!")
	--print(head)
	srv=net.createServer(net.TCP) 
	srv:listen(80,function(conn) 
    conn:on("receive",function(conn,payload)
    -- Filter non HTTP-requests
    if not string.find(payload, "HTTP/") then
    	conn:close();
    else
	    print(payload)
	    print("Send")
	    file.open("head.html")
	    conn:send(file.read())
	    file.close()
	    conn:send("<h1>Say 'Hello' to your SensNetNano :)</h1><ul>")
	    for ssid,v in pairs(t) do
	      local bssid, rssi, authmode, channel = string.match(v, "([^,]+),([^,]+),([^,]+),([^,]+)")
	      conn:send("<li>"..bssid.."\t  "..rssi.."\t"..channel.."</li>")
	    end
	    conn:send("</ul>")
	    conn:close()
    end
    end)
end)
end

function main()
end

function listap(t)
      --for k,v in pairs(t) do
      --   print(k.." : "..v)
      --end
      --wifi.sta.config("AndroidAPj","hallohallo")
	  --tmr.alarm(1,1000, 1, function() if wifi.sta.getip()==nil then print(" Wait for IP address!") else tmr.stop(1) print("ip: " .. wifi.sta.getip()) connectedMain() end end)
      wifi.setmode(wifi.SOFTAP)
      cfg={}
      cfg.ssid="SensNetNano"
  	  cfg.auth=wifi.ap.AUTH_OPEN
  	  ipcfg = {
  	  	ip="192.168.1.1",
  		netmask="255.255.255.0",
  		gateway="192.168.1.1"
      }
      wifi.ap.config(cfg)
      wifi.ap.setip(ipcfg)
      dhcp_config ={}
	  dhcp_config.start = "192.168.1.100"
	  wifi.ap.dhcp.config(dhcp_config)
	  wifi.ap.dhcp.start()
	  connectedMain(t)
    end


l = file.list();
for k,v in pairs(l) do
   if k == "stations" then
   		print("Station list found!")
   		main()
   end
end
wifi.setmode(wifi.STATION)
wifi.sta.getap(1, listap)

#!/bin/bash
echo "📊 Log Monitoring Started"
echo "========================"
echo "💡 Run API tests in another terminal"
echo ""

if [ -f "../logs/application.log" ]; then
    tail -f ../logs/application.log
else
    echo "❌ Log file not found! Make sure application is running."
fi

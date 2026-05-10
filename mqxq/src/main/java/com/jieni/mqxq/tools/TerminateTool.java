package com.jieni.mqxq.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * 终止工具（作用是让自主规划智能体能够合理地中断）
 */
public class TerminateTool {

    @Tool(description = "End task when completed or cannot proceed")
    public String doTerminate() {
        return "任务结束";
    }
}

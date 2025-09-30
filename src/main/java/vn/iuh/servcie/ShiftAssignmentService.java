package vn.iuh.servcie;

import vn.iuh.entity.ShiftAssignment;

public interface ShiftAssignmentService {
    ShiftAssignment getShiftAssignmentByID(String id);
    ShiftAssignment createShiftAssignment(ShiftAssignment shiftAssignment);
    ShiftAssignment updateShiftAssignment(ShiftAssignment shiftAssignment);
    boolean deleteShiftAssignmentByID(String id);
}
package softuni.exam.domain.dtos;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

@XmlRootElement(name = "teams")
@XmlAccessorType(FIELD)
public class TeamSeedRootDto {

    @XmlElement(name = "team")
    List<TeamSeedDto> teamSeedDtos;

    public TeamSeedRootDto() {
    }

    public List<TeamSeedDto> getTeamSeedDtos() {
        return teamSeedDtos;
    }

    public void setTeamSeedDtos(List<TeamSeedDto> teamSeedDtos) {
        this.teamSeedDtos = teamSeedDtos;
    }
}
